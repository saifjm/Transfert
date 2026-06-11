package com.smi.mstr.transfer.domain.entity;

import com.smi.mstr.transfer.domain.enums.OperationEventType;
import com.smi.mstr.transfer.domain.enums.TransferOperationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "TR_OPERATION_EVENT")
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
            sequenceName = "SEQ_TR_OPERATION_EVENT",
            allocationSize = 1
    )
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REF_OPERATION", nullable = false)
    private MvtTrOperation operation;

    @Enumerated(EnumType.STRING)
    @Column(name = "EVENT_TYPE", nullable = false, length = 50)
    private OperationEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "OLD_STATUS", length = 1)
    private TransferOperationStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "NEW_STATUS", length = 1)
    private TransferOperationStatus newStatus;

    @Column(name = "ACTOR_USER_ID", nullable = false, length = 50)
    private String actorUserId;

    @Column(name = "ACTOR_ROLE", length = 50)
    private String actorRole;

    @Column(name = "ACTION_AT", nullable = false)
    private LocalDateTime actionAt;

    @Column(name = "COMMENT_TEXT", length = 1000)
    private String commentText;

    @Lob
    @Column(name = "EVENT_PAYLOAD")
    private String eventPayload;
}
