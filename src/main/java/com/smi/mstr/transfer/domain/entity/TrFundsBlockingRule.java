package com.smi.mstr.transfer.domain.entity;

import com.smi.mstr.transfer.domain.enums.PaymentModalityType;
import com.smi.mstr.transfer.domain.enums.TransferType;
import com.smi.mstr.transfer.domain.enums.YesNoFlag;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "TR_FUNDS_BLOCKING_RULE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrFundsBlockingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tr_funds_block_rule_seq")
    @SequenceGenerator(
            name = "tr_funds_block_rule_seq",
            sequenceName = "SEQ_TR_FUNDS_BLOCKING_RULE",
            allocationSize = 1
    )
    @Column(name = "RULE_ID")
    private Long ruleId;

    @Column(name = "RULE_CODE", nullable = false, length = 50)
    private String ruleCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "ENABLED", nullable = false, length = 1)
    private YesNoFlag enabled;

    @Enumerated(EnumType.STRING)
    @Column(name = "TRANSFER_TYPE", length = 1)
    private TransferType transferType;

    @Enumerated(EnumType.STRING)
    @Column(name = "MODALITY_TYPE", length = 40)
    private PaymentModalityType modalityType;

    @Column(name = "BRANCH_CODE", length = 10)
    private String branchCode;

    @Column(name = "MIN_AMOUNT", precision = 18, scale = 3)
    private BigDecimal minAmount;

    @Column(name = "CURRENCY", length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "REQUIRE_BLOCKING", nullable = false, length = 1)
    private YesNoFlag requireBlocking;

    @Column(name = "PRIORITY_NO", nullable = false)
    private Integer priorityNo;
}