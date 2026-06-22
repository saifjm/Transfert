package com.smi.mstr.transfer.domain.entity;

import com.smi.mstr.transfer.domain.enums.FinancialAgentRole;
import com.smi.mstr.transfer.domain.enums.InterbankInstructionTargetFormat;
import com.smi.mstr.transfer.domain.enums.InterbankInstructionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "TR_INTERBANK_INSTRUCTION", schema = "TRSF")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrInterbankInstruction {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "seq_tr_interbank_instruction"
    )
    @SequenceGenerator(
            name = "seq_tr_interbank_instruction",
            sequenceName = "TRSF.SEQ_TR_INTERBANK_INSTRUCTION",
            allocationSize = 1
    )
    @Column(name = "INSTRUCTION_ID")
    private Long instructionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REF_OPERATION", nullable = false)
    private MvtTrOperation operation;

    @Enumerated(EnumType.STRING)
    @Column(name = "INSTRUCTION_TYPE", nullable = false, length = 60)
    private InterbankInstructionType instructionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "TARGET_FORMAT", nullable = false, length = 20)
    private InterbankInstructionTargetFormat targetFormat;

    @Enumerated(EnumType.STRING)
    @Column(name = "TARGET_AGENT_ROLE", length = 40)
    private FinancialAgentRole targetAgentRole;

    @Column(name = "INSTRUCTION_CODE", length = 35)
    private String instructionCode;

    @Column(name = "INSTRUCTION_TEXT", length = 500)
    private String instructionText;

    @Column(name = "CREATED_BY", length = 100)
    private String createdBy;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;
}