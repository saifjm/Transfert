package com.smi.mstr.transfer.application.workflow.handlers;

import com.smi.mstr.transfer.application.TransferAggregateAssembler;
import com.smi.mstr.transfer.application.TransferOperationCodeResolver;
import com.smi.mstr.transfer.application.TransferReferenceService;
import com.smi.mstr.transfer.application.blocking.PaymentModalityBlockingApplicationService;
import com.smi.mstr.transfer.application.context.WorkflowCommandContext;
import com.smi.mstr.transfer.application.validation.payment.PaymentModalityValidationService;
import com.smi.mstr.transfer.application.workflow.WorkflowNodeCommandHandler;
import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.domain.enums.*;
import com.smi.mstr.transfer.domain.repository.MvtTrOperationRepository;
import com.smi.mstr.transfer.dto.workflow.WorkflowTransferCommandRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AgencyInitiationCommandHandler implements WorkflowNodeCommandHandler {

    private final MvtTrOperationRepository operationRepository;
    private final TransferReferenceService referenceService;
    private final TransferAggregateAssembler aggregateAssembler;
    private final TransferOperationCodeResolver operationCodeResolver;
    private final PaymentModalityValidationService paymentModalityValidationService;
    private final PaymentModalityBlockingApplicationService blockingApplicationService;

    @Override
    public boolean supports(WorkflowNodeCode nodeCode) {
        return nodeCode == WorkflowNodeCode.AGENCY_INITIATION;
    }

    @Override
    public MvtTrOperation create(
            WorkflowTransferCommandRequest request,
            WorkflowCommandContext context
    ) {
        MvtTrOperation operation = new MvtTrOperation();

        operation.setDateOperation(LocalDate.now());
        operation.setDateDossier(LocalDate.now());
        operation.setCreatedAt(LocalDateTime.now());

        operation.setTypeTransfert(request.transferType());
        operation.setStatus(TransferOperationStatus.X);

        operation.setCodeAgence(context.branchCode());
        operation.setWorkflowInstanceId(context.workflowInstanceId());
        operation.setWorkflowTaskId(context.workflowTaskId());
        operation.setCorrelationId(context.correlationId());

        operation.setSourceChannel(OriginChannel.AGENCY);
        operation.setSourceModule("MS-WF");
        operation.setSourceReference(context.workflowInstanceId());

        aggregateAssembler.applyTransferInstruction(operation, request.transferInstruction());
        aggregateAssembler.replaceParties(operation, request.parties());
        aggregateAssembler.replacePaymentModalities(operation, request.paymentModalities());
        aggregateAssembler.replaceRegulatorySupports(operation, request.regulatorySupports());
        operation.setCodeOperation(operationCodeResolver.resolve(request, context));

        paymentModalityValidationService.validateOrThrow(operation, context);

        operation = operationRepository.saveAndFlush(operation);

        operation.setNumDossier(referenceService.generateNumDossier(operation));
        operation.setRefOrdre(referenceService.generateRefOrdre(operation));

        referenceService.ensureUnique(operation);

        if (operation.getEndToEndId() == null || operation.getEndToEndId().isBlank()) {
            operation.setEndToEndId(referenceService.generateEndToEndId(operation));
        }

        if (operation.getUetr() == null || operation.getUetr().isBlank()) {
            operation.setUetr(referenceService.generateUetr());
        }

        if (operation.getTransactionId() == null || operation.getTransactionId().isBlank()) {
            operation.setTransactionId(referenceService.generateTransactionId(operation));
        }

        blockingApplicationService.blockAllOrThrow(operation, context);

        return operation;
    }
}