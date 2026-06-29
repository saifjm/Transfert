package com.smi.mstr.transfer.domain.entity;

import com.smi.mstr.transfer.domain.enums.StatutImputationSupport;
import com.smi.mstr.transfer.domain.enums.StatutReservationSupport;
import com.smi.mstr.transfer.domain.enums.StatutSupportReglementaire;
import com.smi.mstr.transfer.domain.enums.StatutValidationSupport;
import com.smi.mstr.transfer.domain.enums.TypeSupportReglementaire;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "TR_SUPPORT_REGLEMENTAIRE",
        schema = "TRSF",
        indexes = {
                @Index(name = "IX_TR_SUPPORT_OPERATION", columnList = "REF_OPERATION"),
                @Index(name = "IX_TR_SUPPORT_TYPE", columnList = "REF_OPERATION, TYPE_SUPPORT"),
                @Index(name = "IX_TR_SUPPORT_NUM", columnList = "TYPE_SUPPORT, NUM_SUPPORT"),
                @Index(name = "IX_TR_SUPPORT_VALIDATION", columnList = "REF_OPERATION, STATUT_VALIDATION"),
                @Index(name = "IX_TR_SUPPORT_RESERVATION", columnList = "REF_OPERATION, STATUT_RESERVATION"),
                @Index(name = "IX_TR_SUPPORT_IMPUTATION", columnList = "REF_OPERATION, STATUT_IMPUTATION")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrSupportReglementaire {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tr_support_reg_seq")
    @SequenceGenerator(
            name = "tr_support_reg_seq",
            sequenceName = "TRSF.SEQ_TR_SUPPORT_REGLEMENTAIRE",
            allocationSize = 1
    )
    @Column(name = "ID_SUPPORT", nullable = false)
    private Long idSupport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REF_OPERATION", nullable = false)
    private MvtTrOperation operation;

    /*
     * Required because MvtTrOperation uses:
     * @OrderBy("sequenceNo ASC")
     */
    @Column(name = "SEQUENCE_NO", nullable = false)
    private Integer sequenceNo;

    /*
     * TCE, FICHE_INFORMATION, AUTORISATION_BCT, DEROGATION_BCT,
     * CONTRAT_COMMERCIAL, FACTURE, DOCUMENT_TRANSPORT, etc.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE_SUPPORT", nullable = false, length = 40)
    private TypeSupportReglementaire typeSupport;

    @Column(name = "CODE_SUPPORT_BCT")
    private Integer codeSupportBct;

    /*
     * Correction Java naming:
     * ancien champ Java: NumSupport
     * nouveau champ Java: numSupport
     */
    @Column(name = "NUM_SUPPORT", length = 35)
    private String numSupport;

    @Column(name = "DATE_SUPPORT")
    private LocalDate dateSupport;

    @Column(name = "AUTORITE_EMETTRICE", length = 50)
    private String autoriteEmettrice;

    /*
     * Identification réglementaire / reporting BCT
     */
    @Column(name = "NUM_IDENTIFICATION", length = 35)
    private String numIdentification;

    @Column(name = "DATE_IDENTIFICATION")
    private LocalDate dateIdentification;

    @Column(name = "CODE_RD", length = 2)
    private String codeRd;

    @Column(name = "MODE_REGLEMENT")
    private Integer modeReglement;

    @Column(name = "NUM_MESSAGE_SWIFT", length = 35)
    private String numMessageSwift;

    @Column(name = "CODE_BANQUE", length = 10)
    private String codeBanque;

    /*
     * Amounts / reliquats
     */
    @Column(name = "DEVISE_SUPPORT", length = 3)
    private String deviseSupport;

    @Column(name = "MONTANT_AUTORISE", precision = 18, scale = 3)
    private BigDecimal montantAutorise;

    @Column(name = "MONTANT_UTILISE_AVANT", precision = 18, scale = 3)
    private BigDecimal montantUtiliseAvant;

    @Column(name = "MONTANT_RESERVE", precision = 18, scale = 3)
    private BigDecimal montantReserve;

    @Column(name = "MONTANT_UTILISE_COURANT", precision = 18, scale = 3)
    private BigDecimal montantUtiliseCourant;

    @Column(name = "RELIQUAT_AVANT", precision = 18, scale = 3)
    private BigDecimal reliquatAvant;

    @Column(name = "RELIQUAT_APRES", precision = 18, scale = 3)
    private BigDecimal reliquatApres;

    @Column(name = "MONTANT_TND", precision = 18, scale = 3)
    private BigDecimal montantTnd;

    @Column(name = "COURS_CONVERSION", precision = 18, scale = 8)
    private BigDecimal coursConversion;

    /*
     * Lifecycle of the regulatory support
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUT_SUPPORT", nullable = false, length = 30)
    private StatutSupportReglementaire statutSupport;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUT_VALIDATION", nullable = false, length = 30)
    private StatutValidationSupport statutValidation;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUT_RESERVATION", nullable = false, length = 30)
    private StatutReservationSupport statutReservation;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUT_IMPUTATION", nullable = false, length = 30)
    private StatutImputationSupport statutImputation;

    @Column(name = "MESSAGE_VALIDATION", length = 1000)
    private String messageValidation;

    /*
     * External source / snapshot
     */
    @Column(name = "SOURCE_SYSTEM", length = 50)
    private String sourceSystem;

    @Column(name = "SOURCE_REFERENCE", length = 100)
    private String sourceReference;

    @Lob
    @Column(name = "SNAPSHOT_SUPPORT_JSON")
    private String snapshotSupportJson;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (sequenceNo == null) {
            sequenceNo = 1;
        }

        applyDefaultsFromSupportType();

        if (statutSupport == null) {
            statutSupport = StatutSupportReglementaire.DRAFT;
        }

        if (statutValidation == null) {
            statutValidation = validationRequired()
                    ? StatutValidationSupport.PENDING
                    : StatutValidationSupport.NOT_REQUIRED;
        }

        if (statutReservation == null) {
            statutReservation = reservationRequired()
                    ? StatutReservationSupport.NOT_RESERVED
                    : StatutReservationSupport.NOT_REQUIRED;
        }

        if (statutImputation == null) {
            statutImputation = StatutImputationSupport.PENDING;
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

    public boolean isValidated() {
        return statutValidation == StatutValidationSupport.VALIDATED
                || statutValidation == StatutValidationSupport.NOT_REQUIRED;
    }

    public boolean isRejected() {
        return statutValidation == StatutValidationSupport.REJECTED
                || statutSupport == StatutSupportReglementaire.REJECTED;
    }

    public boolean isReserved() {
        return statutReservation == StatutReservationSupport.RESERVED
                || statutReservation == StatutReservationSupport.NOT_REQUIRED;
    }

    public boolean isPartiallyReserved() {
        return statutReservation == StatutReservationSupport.PARTIALLY_RESERVED;
    }

    public boolean isImputed() {
        return statutImputation == StatutImputationSupport.APPLIED
                || statutImputation == StatutImputationSupport.NOT_REQUIRED;
    }

    public boolean isPartiallyImputed() {
        return statutImputation == StatutImputationSupport.PARTIALLY_APPLIED;
    }

    public boolean isTce() {
        return typeSupport == TypeSupportReglementaire.TCE;
    }

    public boolean isFicheInformation() {
        return typeSupport == TypeSupportReglementaire.FICHE_INFORMATION;
    }

    public boolean isAutorisationBct() {
        return typeSupport != null && typeSupport.isAutorisationBct();
    }

    public boolean validationRequired() {
        return typeSupport != null && typeSupport.validationRequired();
    }

    public boolean reservationRequired() {
        return typeSupport != null && typeSupport.reservationRequired();
    }

    public boolean isRegulatoryReportingRelevant() {
        return typeSupport != null && typeSupport.regulatoryReportingRelevant();
    }

    public boolean hasRemainingAmount() {
        return reliquatApres != null
                && reliquatApres.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean hasAvailableReliquat() {
        return reliquatAvant != null
                && reliquatAvant.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean hasNegativeReliquat() {
        return reliquatApres != null
                && reliquatApres.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean hasReservedAmount() {
        return montantReserve != null
                && montantReserve.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean hasCurrentUsedAmount() {
        return montantUtiliseCourant != null
                && montantUtiliseCourant.compareTo(BigDecimal.ZERO) > 0;
    }

    public void markValidated(String message) {
        this.statutValidation = StatutValidationSupport.VALIDATED;
        this.messageValidation = clean(message);
    }

    public void markRejected(String message) {
        this.statutValidation = StatutValidationSupport.REJECTED;
        this.statutSupport = StatutSupportReglementaire.REJECTED;
        this.messageValidation = clean(message);
    }

    public void markReserved(BigDecimal reservedAmount) {
        this.montantReserve = reservedAmount;
        this.statutReservation = StatutReservationSupport.RESERVED;
        this.statutSupport = StatutSupportReglementaire.ACTIVE;

        recalculateReliquatAfterReservation();
    }

    public void markPartiallyReserved(BigDecimal reservedAmount) {
        this.montantReserve = reservedAmount;
        this.statutReservation = StatutReservationSupport.PARTIALLY_RESERVED;
        this.statutSupport = StatutSupportReglementaire.ACTIVE;

        recalculateReliquatAfterReservation();
    }

    public void markReservationFailed(String message) {
        this.statutReservation = StatutReservationSupport.FAILED;
        this.messageValidation = clean(message);
    }

    public void releaseReservation(String message) {
        this.statutReservation = StatutReservationSupport.RELEASED;
        this.montantReserve = BigDecimal.ZERO;
        this.messageValidation = clean(message);

        this.reliquatApres = this.reliquatAvant;
    }

    public void markImputed(BigDecimal usedAmount) {
        this.montantUtiliseCourant = usedAmount;
        this.statutImputation = StatutImputationSupport.APPLIED;
        this.statutSupport = StatutSupportReglementaire.CONSUMED;

        recalculateReliquatAfterCurrentUse();
    }

    public void markPartiallyImputed(BigDecimal usedAmount) {
        this.montantUtiliseCourant = usedAmount;
        this.statutImputation = StatutImputationSupport.PARTIALLY_APPLIED;
        this.statutSupport = StatutSupportReglementaire.ACTIVE;

        recalculateReliquatAfterCurrentUse();
    }

    public void markImputationFailed(String message) {
        this.statutImputation = StatutImputationSupport.FAILED;
        this.messageValidation = clean(message);
    }

    public void recalculateReliquatAfterReservation() {
        if (reliquatAvant == null || montantReserve == null) {
            return;
        }

        reliquatApres = reliquatAvant.subtract(montantReserve);
    }

    public void recalculateReliquatAfterCurrentUse() {
        if (reliquatAvant == null || montantUtiliseCourant == null) {
            return;
        }

        reliquatApres = reliquatAvant.subtract(montantUtiliseCourant);
    }

    private void applyDefaultsFromSupportType() {
        if (typeSupport == null) {
            return;
        }

        if (statutValidation == null && !typeSupport.validationRequired()) {
            statutValidation = StatutValidationSupport.NOT_REQUIRED;
        }

        if (statutReservation == null && !typeSupport.reservationRequired()) {
            statutReservation = StatutReservationSupport.NOT_REQUIRED;
        }
    }

    private void normalizeCodes() {
        deviseSupport = upper(deviseSupport);
        codeRd = upper(codeRd);
        codeBanque = clean(codeBanque);
        numSupport = clean(numSupport);
        numIdentification = clean(numIdentification);
        numMessageSwift = clean(numMessageSwift);
        autoriteEmettrice = clean(autoriteEmettrice);
        sourceSystem = upper(sourceSystem);
        sourceReference = clean(sourceReference);
    }

    private String upper(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }

    private String clean(String value) {
        return value == null ? null : value.trim();
    }
}