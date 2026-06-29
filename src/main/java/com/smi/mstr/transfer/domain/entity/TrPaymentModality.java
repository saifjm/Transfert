package com.smi.mstr.transfer.domain.entity;

import com.smi.mstr.transfer.domain.enums.BlockingStatus;
import com.smi.mstr.transfer.domain.enums.FxType;
import com.smi.mstr.transfer.domain.enums.PaymentImpactStatus;
import com.smi.mstr.transfer.domain.enums.PaymentModalityStatus;
import com.smi.mstr.transfer.domain.enums.PaymentModalityType;
import com.smi.mstr.transfer.domain.enums.PaymentResourceType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "TR_PAYMENT_MODALITY",
        schema = "TRSF",
        indexes = {
                @Index(name = "IX_TR_PAYMOD_OPERATION", columnList = "REF_OPERATION"),
                @Index(name = "IX_TR_PAYMOD_TYPE", columnList = "REF_OPERATION, MODALITY_TYPE"),
                @Index(name = "IX_TR_PAYMOD_BLOCKING", columnList = "REF_OPERATION, BLOCKING_STATUS"),
                @Index(name = "IX_TR_PAYMOD_IMPACT", columnList = "REF_OPERATION, IMPACT_STATUS")
        }
)
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
            sequenceName = "TRSF.SEQ_TR_PAYMENT_MODALITY",
            allocationSize = 1
    )
    @Column(name = "ID_PAYMENT_MODALITY", nullable = false)
    private Long idPaymentModality;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REF_OPERATION", nullable = false)
    private MvtTrOperation operation;

    @Column(name = "SEQUENCE_NO", nullable = false)
    private Integer sequenceNo;

    /*
     * Type de modalité :
     * - TND_FX_PURCHASE_NORMAL
     * - DIRECT_FOREIGN_CURRENCY_ACCOUNT_DEBIT
     * - IMPORT_FINANCING
     * - INTERBANK_COVER
     * etc.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "MODALITY_TYPE", nullable = false, length = 50)
    private PaymentModalityType modalityType;

    /*
     * Quote-part du transfert couverte par cette modalité.
     * Exemple : 50.000000 = 50 %
     */
    @Column(name = "COVERAGE_PERCENTAGE", precision = 9, scale = 6)
    private BigDecimal coveragePercentage;

    /*
     * Montant couvert dans la devise du transfert.
     * Exemple : 25000 EUR sur un transfert total de 50000 EUR.
     */
    @Column(name = "COVERED_TRANSFER_AMOUNT", nullable = false, precision = 18, scale = 3)
    private BigDecimal coveredTransferAmount;

    @Column(name = "COVERED_TRANSFER_CURRENCY", nullable = false, length = 3)
    private String coveredTransferCurrency;

    /*
     * Compte ou ressource débitée.
     *
     * Pour le modèle compact, les comptes peuvent aussi exister comme TrParty
     * avec le rôle DBTR_ACCT / CHARGES_ACCT / SETTLEMENT_ACCT.
     * Ici, on conserve la référence rapide utilisée par la modalité.
     */
    @Column(name = "DEBIT_ACCOUNT_NUMBER", length = 34)
    private String debitAccountNumber;

    @Column(name = "DEBIT_ACCOUNT_CURRENCY", length = 3)
    private String debitAccountCurrency;

    @Column(name = "DEBIT_AMOUNT", precision = 18, scale = 3)
    private BigDecimal debitAmount;

    /*
     * Change
     */
    @Column(name = "FX_REQUIRED", nullable = false, length = 1)
    private String fxRequired;

    @Enumerated(EnumType.STRING)
    @Column(name = "FX_TYPE", length = 30)
    private FxType fxType;

    @Column(name = "FX_RATE", precision = 18, scale = 8)
    private BigDecimal fxRate;

    @Column(name = "FX_RATE_DATE")
    private LocalDateTime fxRateDate;

    @Column(name = "FX_REFERENCE", length = 50)
    private String fxReference;

    /*
     * Ressource de financement :
     * ACCOUNT, FX_CONTRACT, FINANCING_FILE, RECEIVED_FUNDS, INTERBANK_COVER...
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "RESOURCE_TYPE", length = 40)
    private PaymentResourceType resourceType;

    @Column(name = "RESOURCE_REFERENCE", length = 100)
    private String resourceReference;

    /*
     * Blocage / réservation
     */
    @Column(name = "BLOCKING_REQUIRED", nullable = false, length = 1)
    private String blockingRequired;

    @Enumerated(EnumType.STRING)
    @Column(name = "BLOCKING_STATUS", nullable = false, length = 30)
    private BlockingStatus blockingStatus;

    @Column(name = "BLOCKING_REFERENCE", length = 100)
    private String blockingReference;

    @Column(name = "BLOCKED_AMOUNT", precision = 18, scale = 3)
    private BigDecimal blockedAmount;

    @Column(name = "BLOCKED_CURRENCY", length = 3)
    private String blockedCurrency;

    @Column(name = "BLOCKED_AT")
    private LocalDateTime blockedAt;

    /*
     * Impact financier / comptable
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "IMPACT_STATUS", nullable = false, length = 30)
    private PaymentImpactStatus impactStatus;

    @Column(name = "IMPACT_REFERENCE", length = 100)
    private String impactReference;

    @Column(name = "IMPACTED_AT")
    private LocalDateTime impactedAt;

    /*
     * Statut global de la modalité
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "MODALITY_STATUS", nullable = false, length = 30)
    private PaymentModalityStatus modalityStatus;

    /*
     * Snapshot optionnel : réponse du système de compte, change,
     * financement ou blocage.
     */
    @Lob
    @Column(name = "MODALITY_SNAPSHOT_JSON")
    private String modalitySnapshotJson;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (sequenceNo == null) {
            sequenceNo = 1;
        }

        applyDefaultsFromModalityType();

        if (blockingStatus == null) {
            blockingStatus = requiresBlocking()
                    ? BlockingStatus.TO_BLOCK
                    : BlockingStatus.NOT_REQUIRED;
        }

        if (impactStatus == null) {
            impactStatus = PaymentImpactStatus.PENDING;
        }

        if (modalityStatus == null) {
            modalityStatus = PaymentModalityStatus.DRAFT;
        }

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        normalizeCodes();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        normalizeCodes();
    }

    /*
     * -------------------------------------------------------------------------
     * Domain behavior
     * -------------------------------------------------------------------------
     */

    public boolean requiresFx() {
        return isYes(fxRequired);
    }

    public boolean requiresBlocking() {
        return isYes(blockingRequired);
    }

    public boolean isBlocked() {
        return blockingStatus == BlockingStatus.BLOCKED;
    }

    public boolean isPartiallyBlocked() {
        return blockingStatus == BlockingStatus.PARTIALLY_BLOCKED;
    }

    public boolean isExecuted() {
        return impactStatus == PaymentImpactStatus.EXECUTED;
    }

    public boolean isDraft() {
        return modalityStatus == PaymentModalityStatus.DRAFT;
    }

    public boolean isValidated() {
        return modalityStatus == PaymentModalityStatus.VALIDATED;
    }

    public boolean isAccountBased() {
        return resourceType != null && resourceType.isAccountBased();
    }

    public boolean isFinancingBased() {
        return resourceType != null && resourceType.isFinancingBased();
    }

    public boolean isInterbankCoverBased() {
        return resourceType != null && resourceType.isInterbankCoverBased();
    }

    public boolean hasCoveragePercentage() {
        return coveragePercentage != null;
    }

    public boolean hasCoveredAmount() {
        return coveredTransferAmount != null
                && coveredTransferAmount.signum() > 0;
    }

    public boolean hasDebitAccount() {
        return debitAccountNumber != null && !debitAccountNumber.isBlank();
    }

    public boolean hasFxRateOrReference() {
        return fxRate != null
                || (fxReference != null && !fxReference.isBlank());
    }

    public boolean isFxBased() {
        return resourceType != null && resourceType.isFxBased();
    }

    public void markBlocked(
            String blockingReference,
            BigDecimal blockedAmount,
            String blockedCurrency
    ) {
        this.blockingStatus = BlockingStatus.BLOCKED;
        this.blockingReference = blockingReference;
        this.blockedAmount = blockedAmount;
        this.blockedCurrency = upper(blockedCurrency);
        this.blockedAt = LocalDateTime.now();
        this.modalityStatus = PaymentModalityStatus.RESERVED;
    }

    public void markBlockingFailed(String message) {
        this.blockingStatus = BlockingStatus.FAILED;
        this.modalityStatus = PaymentModalityStatus.FAILED;
        this.modalitySnapshotJson = message;
    }

    public void markExecuted(String impactReference) {
        this.impactStatus = PaymentImpactStatus.EXECUTED;
        this.impactReference = impactReference;
        this.impactedAt = LocalDateTime.now();
        this.modalityStatus = PaymentModalityStatus.EXECUTED;
    }

    public void markImpactFailed(String message) {
        this.impactStatus = PaymentImpactStatus.FAILED;
        this.modalityStatus = PaymentModalityStatus.FAILED;
        this.modalitySnapshotJson = message;
    }

    private void applyDefaultsFromModalityType() {
        if (modalityType == null) {
            return;
        }

        if (fxRequired == null) {
            fxRequired = modalityType.fxRequiredByDefault() ? "Y" : "N";
        }

        if (blockingRequired == null) {
            blockingRequired = modalityType.blockingRequiredByDefault() ? "Y" : "N";
        }

        if (resourceType == null) {
            resourceType = modalityType.defaultResourceType();
        }

        if (fxType == null && !requiresFx()) {
            fxType = FxType.NOT_REQUIRED;
        }
    }

    private void normalizeCodes() {
        fxRequired = yesNo(fxRequired);
        blockingRequired = yesNo(blockingRequired);

        coveredTransferCurrency = upper(coveredTransferCurrency);
        debitAccountCurrency = upper(debitAccountCurrency);
        blockedCurrency = upper(blockedCurrency);
        fxReference = clean(fxReference);
        resourceReference = clean(resourceReference);
        blockingReference = clean(blockingReference);
        impactReference = clean(impactReference);
    }

    private String yesNo(String value) {
        if (value == null || value.isBlank()) {
            return "N";
        }

        String normalized = value.trim().toUpperCase();

        if ("O".equals(normalized) || "Y".equals(normalized) || "YES".equals(normalized) || "TRUE".equals(normalized)) {
            return "Y";
        }

        return "N";
    }

    private boolean isYes(String value) {
        return "Y".equalsIgnoreCase(value)
                || "O".equalsIgnoreCase(value);
    }

    private String upper(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }

    private String clean(String value) {
        return value == null ? null : value.trim();
    }
}