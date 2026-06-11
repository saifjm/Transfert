package com.smi.mstr.transfer.domain.entity;

import com.smi.mstr.transfer.domain.enums.PaymentResourceType;
import com.smi.mstr.transfer.domain.enums.PaymentSecurityStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "TR_PAYMENT_SECURITY")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrPaymentSecurity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tr_payment_security_seq")
    @SequenceGenerator(
            name = "tr_payment_security_seq",
            sequenceName = "SEQ_TR_PAYMENT_SECURITY",
            allocationSize = 1
    )
    @Column(name = "SECURITY_ID")
    private Long securityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MODALITY_ID", nullable = false)
    private TrPaymentModality modality;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REF_OPERATION", nullable = false)
    private MvtTrOperation operation;

    @Enumerated(EnumType.STRING)
    @Column(name = "RESOURCE_TYPE", nullable = false, length = 30)
    private PaymentResourceType resourceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "SECURITY_STATUS", nullable = false, length = 30)
    private PaymentSecurityStatus securityStatus;

    @Column(name = "RESOURCE_REF", length = 100)
    private String resourceRef;

    @Column(name = "REQUESTED_AMOUNT", precision = 18, scale = 3)
    private BigDecimal requestedAmount;

    @Column(name = "REQUESTED_CURRENCY", length = 3)
    private String requestedCurrency;

    @Column(name = "FX_RATE", precision = 18, scale = 8)
    private BigDecimal fxRate;

    @Column(name = "COUNTER_VALUE_AMOUNT", precision = 18, scale = 3)
    private BigDecimal counterValueAmount;

    @Column(name = "COUNTER_VALUE_CURRENCY", length = 3)
    private String counterValueCurrency;

    @Column(name = "ESTIMATED_FEES_AMOUNT", precision = 18, scale = 3)
    private BigDecimal estimatedFeesAmount;

    @Column(name = "ESTIMATED_FEES_CURRENCY", length = 3)
    private String estimatedFeesCurrency;

    @Column(name = "SECURED_AMOUNT", precision = 18, scale = 3)
    private BigDecimal securedAmount;

    @Column(name = "SECURED_CURRENCY", length = 3)
    private String securedCurrency;

    @Column(name = "SECURITY_REFERENCE", length = 100)
    private String securityReference;

    @Column(name = "SECURED_AT")
    private LocalDateTime securedAt;

    @Column(name = "RELEASED_AT")
    private LocalDateTime releasedAt;

    @Column(name = "SECURITY_MESSAGE", length = 500)
    private String securityMessage;
}