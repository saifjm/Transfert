package com.smi.mstr.transfer.domain.entity;

import com.smi.mstr.transfer.domain.enums.PaymentModalityType;
import com.smi.mstr.transfer.domain.enums.PaymentResourceAvailabilityStatus;
import com.smi.mstr.transfer.domain.enums.PaymentSecurityStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "TR_PAYMENT_MODALITY")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrPaymentModality {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tr_payment_modality_seq")
    @SequenceGenerator(
            name = "tr_payment_modality_seq",
            sequenceName = "SEQ_TR_PAYMENT_MODALITY",
            allocationSize = 1
    )
    @Column(name = "MODALITY_ID")
    private Long modalityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REF_OPERATION", nullable = false)
    private MvtTrOperation operation;

    @Enumerated(EnumType.STRING)
    @Column(name = "MODALITY_TYPE", nullable = false, length = 50)
    private PaymentModalityType modalityType;

    @Column(name = "SEQUENCE_NO", nullable = false)
    private Integer sequenceNo;

    /**
     * Montant/source réellement utilisé pour financer l'opération.
     * Exemple : montant TND à débiter, montant USD à arbitrer, montant ligne financement.
     */
    @Column(name = "SOURCE_AMOUNT", precision = 18, scale = 3)
    private BigDecimal sourceAmount;

    @Column(name = "SOURCE_CURRENCY", length = 3)
    private String sourceCurrency;

    /**
     * Montant/devise cible de la modalité.
     * En général : montant transféré vers l'étranger.
     */
    @Column(name = "TARGET_AMOUNT", precision = 18, scale = 3)
    private BigDecimal targetAmount;

    @Column(name = "TARGET_CURRENCY", length = 3)
    private String targetCurrency;

    /**
     * Compte débité ou compte support, si la ressource est un compte.
     */
    @Column(name = "DEBIT_ACCOUNT_REF", length = 100)
    private String debitAccountRef;

    @Column(name = "DEBIT_ACCOUNT_CURRENCY", length = 3)
    private String debitAccountCurrency;

    /**
     * Change.
     */
    @Column(name = "FX_MODE", length = 20)
    private String fxMode;

    @Column(name = "FX_RATE", precision = 18, scale = 8)
    private BigDecimal fxRate;

    @Column(name = "FX_DEAL_REF", length = 35)
    private String fxDealRef;

    @Column(name = "FORWARD_CONTRACT_REF", length = 35)
    private String forwardContractRef;

    /**
     * Financement / fonds reçus / couverture.
     */
    @Column(name = "FINANCING_REF", length = 100)
    private String financingRef;

    @Column(name = "RECEIVED_FUNDS_REF", length = 100)
    private String receivedFundsRef;

    @Column(name = "INTERBANK_COVER_REF", length = 100)
    private String interbankCoverRef;

    @Column(name = "COUNTERPARTY_BANK_BIC", length = 11)
    private String counterpartyBankBic;

    @Column(name = "VALUE_DATE")
    private LocalDate valueDate;

    /**
     * Statut résumé de disponibilité.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "AVAILABILITY_STATUS", length = 30)
    private PaymentResourceAvailabilityStatus availabilityStatus;

    @Column(name = "AVAILABLE_AMOUNT", precision = 18, scale = 3)
    private BigDecimal availableAmount;

    @Column(name = "AVAILABLE_CURRENCY", length = 3)
    private String availableCurrency;

    @Column(name = "AVAILABILITY_CHECKED_AT")
    private LocalDateTime availabilityCheckedAt;

    @Column(name = "AVAILABILITY_MESSAGE", length = 500)
    private String availabilityMessage;

    /**
     * Statut résumé de sécurisation.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "SECURITY_STATUS", length = 30)
    private PaymentSecurityStatus securityStatus;

    @OneToMany(
            mappedBy = "modality",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<TrPaymentSecurity> securities = new ArrayList<>();

    public void addSecurity(TrPaymentSecurity security) {
        if (this.securities == null) {
            this.securities = new ArrayList<>();
        }

        this.securities.add(security);
        security.setModality(this);
        security.setOperation(this.operation);
    }

    public void clearSecurities() {
        if (this.securities == null) {
            this.securities = new ArrayList<>();
        } else {
            this.securities.clear();
        }
    }
}