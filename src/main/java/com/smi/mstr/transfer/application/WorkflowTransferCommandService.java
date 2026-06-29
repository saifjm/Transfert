package com.smi.mstr.transfer.application;

import com.smi.mstr.transfer.application.context.WorkflowCommandContext;
import com.smi.mstr.transfer.application.mapper.TransferOperationResponseMapper;
import com.smi.mstr.transfer.application.workflow.WorkflowNodeCommandHandler;
import com.smi.mstr.transfer.application.workflow.WorkflowNodeCommandRegistry;
import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.domain.enums.OperationEventType;
import com.smi.mstr.transfer.domain.enums.TransferOperationStatus;
import com.smi.mstr.transfer.domain.repository.MvtTrOperationRepository;
import com.smi.mstr.transfer.dto.TransferOperationResponse;
import com.smi.mstr.transfer.dto.workflow.WorkflowTransferCommandRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkflowTransferCommandService {

    private final MvtTrOperationRepository operationRepository;
    private final TransferOperationLookupService lookupService;
    private final WorkflowNodeCommandRegistry commandRegistry;
    private final TransferOperationResponseMapper responseMapper;
    private final TransferOperationEventService eventService;

    @Transactional
    public TransferOperationResponse handle(
            WorkflowTransferCommandRequest request,
            WorkflowCommandContext context
    ) {
        validateCommandShape(request, context);

        WorkflowNodeCommandHandler handler =
                commandRegistry.getHandler(context.workflowNodeCode());

        MvtTrOperation operation;

        if (context.workflowNodeCode().isCreationNode()) {
            operation = handler.create(request, context);
        } else {
            operation = lookupService.findByReference(request.operationRef());
            handler.apply(operation, request, context);
        }

        MvtTrOperation saved = operationRepository.save(operation);

        eventService.registerEvent(
                saved,
                OperationEventType.WORKFLOW_DECISION_RECEIVED,
                saved.getStatus(),
                saved.getStatus(),
                context.userId(),
                context.workflowActorRole(),
                "Workflow command applied: " + context.workflowNodeCode(),
                null
        );

        return responseMapper.toResponse(saved);
    }

    private void validateCommandShape(
            WorkflowTransferCommandRequest request,
            WorkflowCommandContext context
    ) {
        if (context.workflowNodeCode().isCreationNode()) {
            if (request.operationRef() != null && !request.operationRef().isBlank()) {
                throw new IllegalArgumentException(
                        "operationRef must be empty for creation node: "
                                + context.workflowNodeCode()
                );
            }
        } else {
            if (request.operationRef() == null || request.operationRef().isBlank()) {
                throw new IllegalArgumentException(
                        "operationRef is required for workflow node: "
                                + context.workflowNodeCode()
                );
            }
        }
    }
}