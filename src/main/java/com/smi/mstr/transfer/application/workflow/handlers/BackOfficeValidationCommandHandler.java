package com.smi.mstr.transfer.application.workflow.handlers;

import com.smi.mstr.transfer.application.FicheInformationService;
import com.smi.mstr.transfer.application.TransferAggregateAssembler;
import com.smi.mstr.transfer.application.blocking.PaymentModalityBlockingApplicationService;
import com.smi.mstr.transfer.application.context.WorkflowCommandContext;
import com.smi.mstr.transfer.application.validation.payment.PaymentModalityValidationService;
import com.smi.mstr.transfer.application.workflow.WorkflowNodeCommandHandler;
import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.domain.enums.TransferOperationStatus;
import com.smi.mstr.transfer.domain.enums.TransferType;
import com.smi.mstr.transfer.domain.enums.WorkflowNodeCode;
import com.smi.mstr.transfer.dto.workflow.WorkflowTransferCommandRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class BackOfficeValidationCommandHandler implements WorkflowNodeCommandHandler {

    private final TransferAggregateAssembler aggregateAssembler;
    private final FicheInformationService ficheInformationService;
    private final PaymentModalityValidationService paymentModalityValidationService;
    private final PaymentModalityBlockingApplicationService blockingApplicationService;

    @Override
    public boolean supports(WorkflowNodeCode nodeCode) {
        return nodeCode == WorkflowNodeCode.BACK_OFFICE_VALIDATION;
    }

    @Override
    public void apply(
            MvtTrOperation operation,
            WorkflowTransferCommandRequest request,
            WorkflowCommandContext context
    ) {
        if (!operation.isEditable()) {
            throw new IllegalStateException(
                    "Only editable operations can be validated. Current status: "
                            + operation.getStatus()
            );
        }

        aggregateAssembler.applyInterbankData(operation, request.interbankData());

        operation.setStatus(TransferOperationStatus.V);
        operation.setDateValidation(LocalDate.now());
        operation.setWorkflowTaskId(context.workflowTaskId());

        if (shouldGenerateFicheInformation(operation, request)) {
            ficheInformationService.generateFor(operation, context);
        }
    }

    private boolean shouldGenerateFicheInformation(
            MvtTrOperation operation,
            WorkflowTransferCommandRequest request
    ) {
        if (request.validationDecision() != null
                && Boolean.TRUE.equals(request.validationDecision().generateFicheInformation())) {
            return true;
        }

        return operation.getTypeTransfert() == TransferType.F;
    }
}