package com.smi.mstr.transfer.application;

import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.domain.entity.TrOperationEvent;
import com.smi.mstr.transfer.domain.enums.OperationEventType;
import com.smi.mstr.transfer.domain.enums.TransferOperationStatus;
import com.smi.mstr.transfer.domain.repository.TrOperationEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransferOperationEventService {

    private final TrOperationEventRepository eventRepository;

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
        TrOperationEvent event = TrOperationEvent.builder()
                .operation(operation)
                .eventType(eventType)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .actorUserId(actorUserId)
                .actorRole(actorRole)
                .actionAt(LocalDateTime.now())
                .commentText(comment)
                .eventPayload(eventPayload)
                .build();

        eventRepository.save(event);
    }
}