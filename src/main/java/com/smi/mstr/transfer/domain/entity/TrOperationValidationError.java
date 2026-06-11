package com.smi.mstr.transfer.domain.entity;

import com.smi.mstr.transfer.domain.enums.ValidationSection;
import com.smi.mstr.transfer.domain.enums.ValidationSeverity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "TR_OPERATION_VALIDATION_ERROR")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrOperationValidationError {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tr_validation_error_seq")
    @SequenceGenerator(
            name = "tr_validation_error_seq",
            sequenceName = "SEQ_TR_VALIDATION_ERROR",
            allocationSize = 1
    )
    @Column(name = "ERROR_ID")
    private Long errorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REF_OPERATION", nullable = false)
    private MvtTrOperation operation;

    @Enumerated(EnumType.STRING)
    @Column(name = "SECTION", nullable = false, length = 30)
    private ValidationSection section;

    @Column(name = "FIELD_PATH", nullable = false, length = 150)
    private String fieldPath;

    @Column(name = "ERROR_CODE", nullable = false, length = 50)
    private String errorCode;

    @Column(name = "ERROR_MESSAGE", nullable = false, length = 500)
    private String errorMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "SEVERITY", nullable = false, length = 20)
    private ValidationSeverity severity;

    @Column(name = "DETECTED_AT", nullable = false)
    private LocalDateTime detectedAt;
}