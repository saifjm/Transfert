package com.smi.mstr.transfer.domain.entity;

import com.smi.mstr.transfer.domain.enums.InterbankRouteType;
import com.smi.mstr.transfer.domain.enums.OriginChannel;
import com.smi.mstr.transfer.domain.enums.PartyRole;
import com.smi.mstr.transfer.domain.enums.SwiftPriority;
import com.smi.mstr.transfer.domain.enums.TransferOperationStatus;
import com.smi.mstr.transfer.domain.enums.TransferType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Entity
@Table(
        name = "TR_OPERATION_MVT",
        schema = "TRSF",
        indexes = {
                @Index(name = "IX_TR_OP_MVT_REF_ORDRE", columnList = "REF_ORDRE"),
                @Index(name = "IX_TR_OP_MVT_STATUS", columnList = "STATUS"),
                @Index(name = "IX_TR_OP_MVT_AGENCE_STATUS", columnList = "CODE_AGENCE, STATUS"),
                @Index(name = "IX_TR_OP_MVT_UETR", columnList = "UETR"),
                @Index(name = "IX_TR_OP_MVT_CORRELATION", columnList = "CORRELATION_ID")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MvtTrOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tr_operation_mvt_seq")
    @SequenceGenerator(
            name = "tr_operation_mvt_seq",
            sequenceName = "TRSF.SEQ_TR_OPERATION_MVT",
            allocationSize = 1
    )
    @Column(name = "REF_OPERATION", nullable = false)
    private Long refOperation;

    /*
     * -------------------------------------------------------------------------
     * 1. Operation identifiers
     * -------------------------------------------------------------------------
     */

    @Column(name = "DATE_OPERATION", nullable = false)
    private LocalDate dateOperation;

    @Column(name = "CODE_OPERATION", nullable = false)
    private Long codeOperation;

    @Column(name = "NUM_DOSSIER", length = 35)
    private String numDossier;

    @Column(name = "DATE_DOSSIER", nullable = false)
    private LocalDate dateDossier;

    @Column(name = "CORRELATION_ID", length = 100)
    private String correlationId;

    @Column(name = "CODE_AGENCE", length = 10)
    private String codeAgence;

    /*
     * -------------------------------------------------------------------------
     * 2. Source and workflow context
     * -------------------------------------------------------------------------
     */

    @Enumerated(EnumType.STRING)
    @Column(name = "SOURCE_CHANNEL", length = 30)
    private OriginChannel sourceChannel;

    @Column(name = "SOURCE_MODULE", length = 50)
    private String sourceModule;

    @Column(name = "SOURCE_REFERENCE", length = 100)
    private String sourceReference;

    @Column(name = "WORKFLOW_INSTANCE_ID", length = 100)
    private String workflowInstanceId;

    @Column(name = "WORKFLOW_TASK_ID", length = 100)
    private String workflowTaskId;

    @Lob
    @Column(name = "WORKFLOW_CONTEXT_JSON")
    private String workflowContextJson;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Long version;

    /*
     * -------------------------------------------------------------------------
     * 3. Transfer instruction
     * -------------------------------------------------------------------------
     */

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE_TRANSFERT", nullable = false, length = 1)
    private TransferType typeTransfert;

    @Column(name = "CODE_NATURE_OPERATION", length = 4)
    private String codeNatureOperation;

    @Column(name = "NUM_AUTORISATION_BCT", length = 35)
    private String numAutorisationBct;

    @Column(name = "DATE_AUTORISATION_BCT")
    private LocalDate dateAutorisationBct;

    @Column(name = "TYPE_DOSSIER_REG", length = 30)
    private String typeDossierReg;

    @Column(name = "NUM_DOSSIER_REG", length = 35)
    private String numDossierReg;

    @Column(name = "DATE_DOSSIER_REG")
    private LocalDate dateDossierReg;

    /*
     * -------------------------------------------------------------------------
     * 4. Party/account shortcut fields
     *
     * In the compact model, detailed parties, accounts and agents are all stored
     * as TrParty rows with different PartyRole values.
     *
     * These fields are only shortcuts for search/reporting/compatibility.
     * They must be synchronized from the TrParty collection by the service layer.
     * -------------------------------------------------------------------------
     */

    @Column(name = "ULTIMATE_DEBTOR_ID")
    private Long ultimateDebtorId;

    @Column(name = "DEBTOR_ID")
    private Long debtorId;

    @Column(name = "CREDITOR_ID")
    private Long creditorId;

    @Column(name = "ULTIMATE_CREDITOR_ID")
    private Long ultimateCreditorId;

    @Column(name = "CREDITOR_AGENT_ID")
    private Long creditorAgentId;

    @Column(name = "INITG_PTY_ID")
    private Long initgPtyId;

    @Column(name = "INSTG_AGT_ID")
    private Long instgAgtId;

    @Column(name = "INSTD_AGT_ID")
    private Long instdAgtId;

    @Column(name = "COVER_AGENT_ID")
    private Long coverAgtId;

    @Column(name = "NO_COMPTE_CREDITOR", length = 100)
    private String noCompteCreditor;

    @Column(name = "NO_COMPTE_COMMISSION", length = 100)
    private String noCompteCommission;

    /*
     * -------------------------------------------------------------------------
     * 5. ISO 20022 / SWIFT references
     * -------------------------------------------------------------------------
     */

    @Column(name = "END_TO_END_ID", length = 35)
    private String endToEndId;

    @Column(name = "TRANSACTION_ID", length = 35)
    private String transactionId;

    @Column(name = "REF_ORDRE", length = 35)
    private String refOrdre;

    @Column(name = "UETR", length = 36)
    private String uetr;

    /*
     * -------------------------------------------------------------------------
     * 6. Amounts and currencies
     * -------------------------------------------------------------------------
     */

    @Column(name = "MNT_ORDRE", precision = 18, scale = 3)
    private BigDecimal mntOrdre;

    @Column(name = "CODE_DEVISE_ORDRE", length = 3)
    private String codeDeviseOrdre;

    @Column(name = "MNT_DEVISE", precision = 18, scale = 3)
    private BigDecimal mntDevise;

    @Column(name = "CODE_DEVISE", length = 3)
    private String codeDevise;

    @Column(name = "DATE_VALEUR_TRANSFERT")
    private LocalDate dateValeurTransfert;

    @Column(name = "COURS_CONVERSION", precision = 18, scale = 8)
    private BigDecimal coursConversion;

    @Column(name = "CONTRE_VALEUR_TND", precision = 18, scale = 3)
    private BigDecimal contreValeurTnd;

    /*
     * -------------------------------------------------------------------------
     * 7. Payment instruction data
     * -------------------------------------------------------------------------
     */

    @Enumerated(EnumType.STRING)
    @Column(name = "SWIFT_PRIORITY", length = 1)
    private SwiftPriority swiftPriority;

    @Column(name = "SERVICE_LEVEL_CODE", length = 10)
    private String serviceLevelCode;

    @Column(name = "LOCAL_INSTRUMENT_CODE", length = 35)
    private String localInstrumentCode;

    @Column(name = "CATEGORY_PURPOSE_CODE", length = 35)
    private String categoryPurposeCode;

    @Column(name = "PURPOSE_CODE", length = 35)
    private String purposeCode;

    @Column(name = "PURPOSE_PROPRIETARY", length = 140)
    private String purposeProprietary;

    @Column(name = "REMITTANCE_UNSTRUCTURED", length = 560)
    private String remittanceUnstructured;

    @Column(name = "CHARGE_BEARER", length = 4)
    private String chargeBearer;

    @Lob
    @Column(name = "GARNISHMENT")
    private String garnishment;

    /*
     * -------------------------------------------------------------------------
     * 8. Interbank routing summary
     *
     * In the compact model:
     * - agents are stored in TrParty with PartyRole DBTR_AGT, CDTR_AGT,
     *   INSTG_AGT, INSTD_AGT, INTRMY_AGT_1, COVER_AGT, etc.
     * - only the selected route summary remains on the operation.
     * -------------------------------------------------------------------------
     */

    @Enumerated(EnumType.STRING)
    @Column(name = "ROUTE_TYPE", length = 30)
    private InterbankRouteType routeType;

    @Column(name = "COVER_REQUIRED", length = 1)
    private String coverRequired;

    @Column(name = "SETTLEMENT_METHOD", length = 10)
    private String settlementMethod;

    @Column(name = "SETTLEMENT_ACCOUNT_REF", length = 100)
    private String settlementAccountRef;

    @Lob
    @Column(name = "INTERBANK_SNAPSHOT_JSON")
    private String interbankSnapshotJson;

    /*
     * -------------------------------------------------------------------------
     * 9. Lifecycle
     * -------------------------------------------------------------------------
     */

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 1)
    private TransferOperationStatus status;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "DATE_VALIDATION")
    private LocalDate dateValidation;

    /*
     * -------------------------------------------------------------------------
     * 10. Child domain models
     * -------------------------------------------------------------------------
     */

    @OneToMany(
            mappedBy = "operation",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("partyRole ASC, sequenceNo ASC")
    @BatchSize(size = 50)
    @Builder.Default
    private List<TrParty> parties = new ArrayList<>();

    @OneToMany(
            mappedBy = "operation",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("sequenceNo ASC")
    @BatchSize(size = 50)
    @Builder.Default
    private List<TrPaymentModality> paymentModalities = new ArrayList<>();

    @OneToMany(
            mappedBy = "operation",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("sequenceNo ASC")
    @BatchSize(size = 50)
    @Builder.Default
    private List<TrSupportReglementaire> supportsReglementaires = new ArrayList<>();

    /*
     * -------------------------------------------------------------------------
     * 11. JPA lifecycle callbacks
     * -------------------------------------------------------------------------
     */

    @PrePersist
    protected void onCreate() {
        LocalDate today = LocalDate.now();

        if (dateOperation == null) {
            dateOperation = today;
        }

        if (dateDossier == null) {
            dateDossier = today;
        }

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (status == null) {
            status = TransferOperationStatus.X;
        }

        if (coverRequired == null) {
            coverRequired = "N";
        }
    }

    /*
     * -------------------------------------------------------------------------
     * 12. Domain behavior
     * -------------------------------------------------------------------------
     */

    public boolean isEditable() {
        return status == TransferOperationStatus.X;
    }

    public boolean isValidated() {
        return status == TransferOperationStatus.V;
    }

    public boolean isAppliedOrExecuted() {
        return status == TransferOperationStatus.A;
    }

    public boolean requiresCover() {
        return "O".equalsIgnoreCase(coverRequired)
                || "Y".equalsIgnoreCase(coverRequired);
    }

    public boolean hasParties() {
        return parties != null && !parties.isEmpty();
    }

    public boolean hasPaymentModalities() {
        return paymentModalities != null && !paymentModalities.isEmpty();
    }

    public boolean hasSupportsReglementaires() {
        return supportsReglementaires != null && !supportsReglementaires.isEmpty();
    }

    /*
     * -------------------------------------------------------------------------
     * 13. Party management
     * -------------------------------------------------------------------------
     */

    public void addParty(TrParty party) {
        if (party == null) {
            return;
        }

        ensurePartiesInitialized();

        parties.add(party);
        party.setOperation(this);
    }

    public void addParties(List<TrParty> partiesToAdd) {
        if (partiesToAdd == null || partiesToAdd.isEmpty()) {
            return;
        }

        partiesToAdd.forEach(this::addParty);
    }

    public Optional<TrParty> findParty(PartyRole role) {
        if (role == null || parties == null) {
            return Optional.empty();
        }

        return parties.stream()
                .filter(party -> party.getPartyRole() == role)
                .findFirst();
    }

    public List<TrParty> findParties(PartyRole role) {
        if (role == null || parties == null) {
            return List.of();
        }

        return parties.stream()
                .filter(party -> party.getPartyRole() == role)
                .toList();
    }

    public void removePartiesByRole(PartyRole... roles) {
        if (parties == null || roles == null || roles.length == 0) {
            return;
        }

        List<PartyRole> roleList = Arrays.asList(roles);

        parties.removeIf(party -> {
            boolean shouldRemove = roleList.contains(party.getPartyRole());

            if (shouldRemove) {
                party.setOperation(null);
            }

            return shouldRemove;
        });
    }

    public void replaceParty(PartyRole role, TrParty party) {
        removePartiesByRole(role);

        if (party != null) {
            party.setPartyRole(role);
            addParty(party);
        }
    }

    public void clearParties() {
        ensurePartiesInitialized();

        parties.forEach(party -> party.setOperation(null));
        parties.clear();
    }

    /*
     * -------------------------------------------------------------------------
     * 14. Payment modality management
     * -------------------------------------------------------------------------
     */

    public void addPaymentModality(TrPaymentModality modality) {
        if (modality == null) {
            return;
        }

        ensurePaymentModalitiesInitialized();

        paymentModalities.add(modality);
        modality.setOperation(this);
    }

    public void addPaymentModalities(List<TrPaymentModality> modalitiesToAdd) {
        if (modalitiesToAdd == null || modalitiesToAdd.isEmpty()) {
            return;
        }

        modalitiesToAdd.forEach(this::addPaymentModality);
    }

    public void replacePaymentModalities(List<TrPaymentModality> newModalities) {
        clearPaymentModalities();
        addPaymentModalities(newModalities);
    }

    public void clearPaymentModalities() {
        ensurePaymentModalitiesInitialized();

        paymentModalities.forEach(modality -> modality.setOperation(null));
        paymentModalities.clear();
    }

    /*
     * -------------------------------------------------------------------------
     * 15. Regulatory support management
     * -------------------------------------------------------------------------
     */

    public void addSupportReglementaire(TrSupportReglementaire support) {
        if (support == null) {
            return;
        }

        ensureSupportsReglementairesInitialized();

        supportsReglementaires.add(support);
        support.setOperation(this);
    }

    public void addSupportsReglementaires(List<TrSupportReglementaire> supportsToAdd) {
        if (supportsToAdd == null || supportsToAdd.isEmpty()) {
            return;
        }

        supportsToAdd.forEach(this::addSupportReglementaire);
    }

    public void replaceSupportsReglementaires(List<TrSupportReglementaire> newSupports) {
        clearSupportsReglementaires();
        addSupportsReglementaires(newSupports);
    }

    public void clearSupportsReglementaires() {
        ensureSupportsReglementairesInitialized();

        supportsReglementaires.forEach(support -> support.setOperation(null));
        supportsReglementaires.clear();
    }

    /*
     * -------------------------------------------------------------------------
     * 16. Internal collection helpers
     * -------------------------------------------------------------------------
     */

    private void ensurePartiesInitialized() {
        if (parties == null) {
            parties = new ArrayList<>();
        }
    }

    private void ensurePaymentModalitiesInitialized() {
        if (paymentModalities == null) {
            paymentModalities = new ArrayList<>();
        }
    }

    private void ensureSupportsReglementairesInitialized() {
        if (supportsReglementaires == null) {
            supportsReglementaires = new ArrayList<>();
        }
    }
}