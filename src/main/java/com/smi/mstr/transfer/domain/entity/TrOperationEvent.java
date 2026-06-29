package com.smi.mstr.transfer.domain.entity;

import com.smi.mstr.transfer.domain.enums.OperationEventType;
import com.smi.mstr.transfer.domain.enums.TransferOperationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "TR_OPERATION_EVENT",
        schema = "TRSF",
        indexes = {
                @Index(name = "IX_TR_OP_EVT_OPERATION", columnList = "REF_OPERATION"),
                @Index(name = "IX_TR_OP_EVT_TYPE", columnList = "REF_OPERATION, EVENT_TYPE"),
                @Index(name = "IX_TR_OP_EVT_ACTION_AT", columnList = "ACTION_AT"),
                @Index(name = "IX_TR_OP_EVT_ACTOR", columnList = "ACTOR_USER_ID"),
                @Index(name = "IX_TR_OP_EVT_WF_TASK", columnList = "WORKFLOW_TASK_ID"),
                @Index(name = "IX_TR_OP_EVT_CORRELATION", columnList = "CORRELATION_ID")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrOperationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tr_operation_event_seq")
    @SequenceGenerator(
            name = "tr_operation_event_seq",
            sequenceName = "TRSF.SEQ_TR_OPERATION_EVENT",
            allocationSize = 1
    )
    @Column(name = "ID", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REF_OPERATION", nullable = false)
    private MvtTrOperation operation;

    /*
     * Event type:
     * OPERATION_CREATED, DRAFT_SAVED, ORDER_UPDATED,
     * PAYMENT_MODALITIES_UPDATED, SUPPORT_VALIDATED,
     * INTERBANK_CHAIN_UPDATED, etc.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "EVENT_TYPE", nullable = false, length = 80)
    private OperationEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "OLD_STATUS", length = 1)
    private TransferOperationStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "NEW_STATUS", length = 1)
    private TransferOperationStatus newStatus;

    /*
     * Actor / origin
     */
    @Column(name = "ACTOR_USER_ID", nullable = false, length = 100)
    private String actorUserId;

    @Column(name = "ACTOR_ROLE", length = 80)
    private String actorRole;

    @Column(name = "ACTOR_BRANCH_CODE", length = 10)
    private String actorBranchCode;

    /*
     * Workflow / correlation
     */
    @Column(name = "WORKFLOW_INSTANCE_ID", length = 100)
    private String workflowInstanceId;

    @Column(name = "WORKFLOW_TASK_ID", length = 100)
    private String workflowTaskId;

    @Column(name = "CORRELATION_ID", length = 100)
    private String correlationId;

    @Column(name = "IDEMPOTENCY_KEY", length = 120)
    private String idempotencyKey;

    /*
     * Event timestamp
     */
    @Column(name = "ACTION_AT", nullable = false)
    private LocalDateTime actionAt;

    /*
     * Human-readable comment
     */
    @Column(name = "COMMENT_TEXT", length = 1000)
    private String commentText;

    /*
     * Technical payload / snapshot.
     * Can contain JSON with changed fields, external response, validation report, etc.
     */
    @Lob
    @Column(name = "EVENT_PAYLOAD")
    private String eventPayload;

    @PrePersist
    protected void onCreate() {
        if (actionAt == null) {
            actionAt = LocalDateTime.now();
        }

        if (actorUserId == null || actorUserId.isBlank()) {
            actorUserId = "SYSTEM";
        }

        if (actorRole == null || actorRole.isBlank()) {
            actorRole = "SYSTEM";
        }

        normalize();
    }

    @PreUpdate
    protected void onUpdate() {
        normalize();
    }

    private void normalize() {
        actorUserId = clean(actorUserId);
        actorRole = upper(actorRole);
        actorBranchCode = clean(actorBranchCode);
        workflowInstanceId = clean(workflowInstanceId);
        workflowTaskId = clean(workflowTaskId);
        correlationId = clean(correlationId);
        idempotencyKey = clean(idempotencyKey);
        commentText = clean(commentText);
    }

    private String clean(String value) {
        return value == null ? null : value.trim();
    }

    private String upper(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }
}