package com.smi.mstr.transfer.application;

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

    private static final String SYSTEM_ACTOR = "SYSTEM";
    private static final String SYSTEM_ROLE = "SYSTEM";

    private final TrOperationEventRepository eventRepository;

    @Transactional
    public void registerEvent(
            MvtTrOperation operation,
            OperationEventType eventType,
            TransferOperationStatus oldStatus,
            TransferOperationStatus newStatus,
            String actorUserId,
            String actorRole,
            String comment,
            String eventPayload
    ) {
        validate(operation, eventType);

        TrOperationEvent event = TrOperationEvent.builder()
                .operation(operation)
                .eventType(eventType)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .actorUserId(resolveActor(actorUserId))
                .actorRole(resolveRole(actorRole))
                .actionAt(LocalDateTime.now())
                .commentText(comment)
                .eventPayload(eventPayload)
                .build();

        eventRepository.save(event);
    }

    public void registerEvent(
            MvtTrOperation operation,
            OperationEventType eventType,
            String actorUserId,
            String actorRole,
            String comment
    ) {
        registerEvent(
                operation,
                eventType,
                operation == null ? null : operation.getStatus(),
                operation == null ? null : operation.getStatus(),
                actorUserId,
                actorRole,
                comment,
                null
        );
    }

    private void validate(
            MvtTrOperation operation,
            OperationEventType eventType
    ) {
        if (operation == null) {
            throw new IllegalArgumentException("Operation is required to register an event.");
        }

        if (operation.getRefOperation() == null) {
            throw new IllegalArgumentException("Operation REF_OPERATION is required to register an event.");
        }

        if (eventType == null) {
            throw new IllegalArgumentException("Operation event type is required.");
        }
    }

    private String resolveActor(String actorUserId) {
        return actorUserId == null || actorUserId.isBlank()
                ? SYSTEM_ACTOR
                : actorUserId;
    }

    private String resolveRole(String actorRole) {
        return actorRole == null || actorRole.isBlank()
                ? SYSTEM_ROLE
                : actorRole;
    }
}