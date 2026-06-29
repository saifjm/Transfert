package com.smi.mstr.transfer.domain.repository;

import com.smi.mstr.transfer.domain.entity.TrOperationEvent;
import com.smi.mstr.transfer.domain.enums.OperationEventType;
import com.smi.mstr.transfer.domain.enums.TransferOperationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface TrOperationEventRepository extends JpaRepository<TrOperationEvent, Long> {

    List<TrOperationEvent> findByOperationRefOperationOrderByActionAtDesc(
            Long refOperation
    );

    Page<TrOperationEvent> findByOperationRefOperationOrderByActionAtDesc(
            Long refOperation,
            Pageable pageable
    );

    List<TrOperationEvent> findByOperationRefOperationAndEventTypeOrderByActionAtDesc(
            Long refOperation,
            OperationEventType eventType
    );

    List<TrOperationEvent> findByOperationRefOperationAndEventTypeInOrderByActionAtDesc(
            Long refOperation,
            Collection<OperationEventType> eventTypes
    );

    List<TrOperationEvent> findByActorUserIdOrderByActionAtDesc(
            String actorUserId
    );

    Page<TrOperationEvent> findByActorUserIdOrderByActionAtDesc(
            String actorUserId,
            Pageable pageable
    );

    List<TrOperationEvent> findByActorRoleOrderByActionAtDesc(
            String actorRole
    );

    List<TrOperationEvent> findByWorkflowInstanceIdOrderByActionAtDesc(
            String workflowInstanceId
    );

    List<TrOperationEvent> findByWorkflowTaskIdOrderByActionAtDesc(
            String workflowTaskId
    );

    List<TrOperationEvent> findByCorrelationIdOrderByActionAtDesc(
            String correlationId
    );

    List<TrOperationEvent> findByNewStatusOrderByActionAtDesc(
            TransferOperationStatus newStatus
    );

    List<TrOperationEvent> findByActionAtBetweenOrderByActionAtDesc(
            LocalDateTime from,
            LocalDateTime to
    );

    boolean existsByIdempotencyKey(
            String idempotencyKey
    );

    long countByOperationRefOperation(
            Long refOperation
    );
}