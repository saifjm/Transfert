package com.smi.mstr.transfer.application;

import com.smi.mstr.transfer.application.context.WorkflowCommandContext;
import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.domain.entity.TrOperationEvent;
import com.smi.mstr.transfer.domain.enums.OperationEventType;
import com.smi.mstr.transfer.domain.enums.TransferOperationStatus;
import com.smi.mstr.transfer.domain.repository.TrOperationEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransferOperationEventService {

    private static final String SYSTEM_USER = "SYSTEM";
    private static final String SYSTEM_ROLE = "SYSTEM";

    private final TrOperationEventRepository eventRepository;

    @Transactional
    public TrOperationEvent registerEvent(
            MvtTrOperation operation,
            OperationEventType eventType,
            TransferOperationStatus oldStatus,
            TransferOperationStatus newStatus,
            String actorUserId,
            String actorRole,
            String comment,
            String eventPayload
    ) {
        validateOperation(operation);
        validateEventType(eventType);

        TrOperationEvent event = TrOperationEvent.builder()
                .operation(operation)
                .eventType(eventType)
                .oldStatus(oldStatus)
                .newStatus(newStatus)

                .actorUserId(resolveActorUserId(actorUserId))
                .actorRole(resolveActorRole(actorRole))
                .actorBranchCode(operation.getCodeAgence())

                .workflowInstanceId(operation.getWorkflowInstanceId())
                .workflowTaskId(operation.getWorkflowTaskId())
                .correlationId(operation.getCorrelationId())

                .actionAt(LocalDateTime.now())
                .commentText(clean(comment))
                .eventPayload(eventPayload)
                .build();

        return eventRepository.save(event);
    }

    @Transactional
    public TrOperationEvent registerWorkflowEvent(
            MvtTrOperation operation,
            OperationEventType eventType,
            TransferOperationStatus oldStatus,
            TransferOperationStatus newStatus,
            WorkflowCommandContext context,
            String comment,
            String eventPayload
    ) {
        validateOperation(operation);
        validateEventType(eventType);

        if (isDuplicateIdempotentEvent(context)) {
            return null;
        }

        TrOperationEvent event = TrOperationEvent.builder()
                .operation(operation)
                .eventType(eventType)
                .oldStatus(oldStatus)
                .newStatus(newStatus)

                .actorUserId(resolveActorUserId(context == null ? null : context.userId()))
                .actorRole(resolveActorRole(context == null ? null : context.workflowActorRole()))
                .actorBranchCode(resolveBranchCode(operation, context))

                .workflowInstanceId(resolveWorkflowInstanceId(operation, context))
                .workflowTaskId(resolveWorkflowTaskId(operation, context))
                .correlationId(resolveCorrelationId(operation, context))
                .idempotencyKey(context == null ? null : clean(context.idempotencyKey()))

                .actionAt(LocalDateTime.now())
                .commentText(clean(comment))
                .eventPayload(eventPayload)
                .build();

        return eventRepository.save(event);
    }

    @Transactional
    public TrOperationEvent registerStatusChange(
            MvtTrOperation operation,
            TransferOperationStatus oldStatus,
            TransferOperationStatus newStatus,
            String actorUserId,
            String actorRole,
            String comment
    ) {
        return registerEvent(
                operation,
                OperationEventType.STATUS_CHANGED,
                oldStatus,
                newStatus,
                actorUserId,
                actorRole,
                comment,
                null
        );
    }

    @Transactional
    public TrOperationEvent registerStatusChange(
            MvtTrOperation operation,
            TransferOperationStatus oldStatus,
            TransferOperationStatus newStatus,
            WorkflowCommandContext context,
            String comment
    ) {
        return registerWorkflowEvent(
                operation,
                OperationEventType.STATUS_CHANGED,
                oldStatus,
                newStatus,
                context,
                comment,
                null
        );
    }

    @Transactional
    public TrOperationEvent registerOperationCreated(
            MvtTrOperation operation,
            WorkflowCommandContext context
    ) {
        return registerWorkflowEvent(
                operation,
                OperationEventType.OPERATION_CREATED,
                null,
                operation.getStatus(),
                context,
                "Transfer operation created.",
                null
        );
    }

    @Transactional
    public TrOperationEvent registerOperationUpdated(
            MvtTrOperation operation,
            WorkflowCommandContext context,
            String comment,
            String eventPayload
    ) {
        return registerWorkflowEvent(
                operation,
                OperationEventType.ORDER_UPDATED,
                operation.getStatus(),
                operation.getStatus(),
                context,
                comment,
                eventPayload
        );
    }

    @Transactional
    public TrOperationEvent registerValidationPassed(
            MvtTrOperation operation,
            TransferOperationStatus oldStatus,
            WorkflowCommandContext context,
            String comment,
            String eventPayload
    ) {
        return registerWorkflowEvent(
                operation,
                OperationEventType.OPERATION_VALIDATED,
                oldStatus,
                operation.getStatus(),
                context,
                comment,
                eventPayload
        );
    }

    @Transactional
    public TrOperationEvent registerValidationFailed(
            MvtTrOperation operation,
            WorkflowCommandContext context,
            String comment,
            String eventPayload
    ) {
        return registerWorkflowEvent(
                operation,
                OperationEventType.VALIDATION_FAILED,
                operation.getStatus(),
                operation.getStatus(),
                context,
                comment,
                eventPayload
        );
    }

    @Transactional
    public TrOperationEvent registerTechnicalException(
            MvtTrOperation operation,
            WorkflowCommandContext context,
            Exception exception
    ) {
        String payload = buildExceptionPayload(exception);

        return registerWorkflowEvent(
                operation,
                OperationEventType.TECHNICAL_EXCEPTION,
                operation == null ? null : operation.getStatus(),
                operation == null ? null : operation.getStatus(),
                context,
                exception == null ? "Technical exception." : exception.getMessage(),
                payload
        );
    }

    @Transactional(readOnly = true)
    public boolean isAlreadyProcessed(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return false;
        }

        return eventRepository.existsByIdempotencyKey(idempotencyKey.trim());
    }

    private boolean isDuplicateIdempotentEvent(WorkflowCommandContext context) {
        if (context == null || context.idempotencyKey() == null || context.idempotencyKey().isBlank()) {
            return false;
        }

        return eventRepository.existsByIdempotencyKey(context.idempotencyKey().trim());
    }

    private void validateOperation(MvtTrOperation operation) {
        if (operation == null) {
            throw new IllegalArgumentException("Operation is required to register an event.");
        }

        if (operation.getRefOperation() == null) {
            throw new IllegalStateException("Operation must be persisted before registering an event.");
        }
    }

    private void validateEventType(OperationEventType eventType) {
        if (eventType == null) {
            throw new IllegalArgumentException("Event type is required.");
        }
    }

    private String resolveActorUserId(String actorUserId) {
        if (actorUserId == null || actorUserId.isBlank()) {
            return SYSTEM_USER;
        }

        return actorUserId.trim();
    }

    private String resolveActorRole(String actorRole) {
        if (actorRole == null || actorRole.isBlank()) {
            return SYSTEM_ROLE;
        }

        return actorRole.trim().toUpperCase();
    }

    private String resolveBranchCode(
            MvtTrOperation operation,
            WorkflowCommandContext context
    ) {
        if (context != null && context.branchCode() != null && !context.branchCode().isBlank()) {
            return context.branchCode().trim();
        }

        return operation.getCodeAgence();
    }

    private String resolveWorkflowInstanceId(
            MvtTrOperation operation,
            WorkflowCommandContext context
    ) {
        if (context != null && context.workflowInstanceId() != null && !context.workflowInstanceId().isBlank()) {
            return context.workflowInstanceId().trim();
        }

        return operation.getWorkflowInstanceId();
    }

    private String resolveWorkflowTaskId(
            MvtTrOperation operation,
            WorkflowCommandContext context
    ) {
        if (context != null && context.workflowTaskId() != null && !context.workflowTaskId().isBlank()) {
            return context.workflowTaskId().trim();
        }

        return operation.getWorkflowTaskId();
    }

    private String resolveCorrelationId(
            MvtTrOperation operation,
            WorkflowCommandContext context
    ) {
        if (context != null && context.correlationId() != null && !context.correlationId().isBlank()) {
            return context.correlationId().trim();
        }

        return operation.getCorrelationId();
    }

    private String buildExceptionPayload(Exception exception) {
        if (exception == null) {
            return null;
        }

        String message = exception.getMessage() == null ? "" : escapeJson(exception.getMessage());

        return """
                {
                  "exceptionType": "%s",
                  "message": "%s"
                }
                """.formatted(
                exception.getClass().getName(),
                message
        );
    }

    private String clean(String value) {
        return value == null ? null : value.trim();
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}