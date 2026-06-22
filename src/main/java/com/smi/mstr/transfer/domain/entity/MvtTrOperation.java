package com.smi.mstr.transfer.domain.entity;

import com.smi.mstr.transfer.domain.enums.OriginChannel;
import com.smi.mstr.transfer.domain.enums.SwiftPriority;
import com.smi.mstr.transfer.domain.enums.TransferOperationStatus;
import com.smi.mstr.transfer.domain.enums.TransferType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.BatchSize;

import com.smi.mstr.transfer.domain.entity.TrPaymentModality;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "TR_OPERATION_MVT", schema = "TRSF")
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

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE_TRANSFERT", nullable = false, length = 1)
    private TransferType typeTransfert;

    @Column(name = "ULTIMATE_DEBTOR_ID")
    private Long ultimateDebtorId;

    @Column(name = "DEBTOR_ID")
    private Long debtorId;

    @Column(name = "NO_COMPTE_COMMISSION", length = 100)
    private String noCompteCommission;

    @Column(name = "CREDITOR_ID")
    private Long creditorId;

    @Column(name = "NO_COMPTE_CREDITOR", length = 100)
    private String noCompteCreditor;

    @Column(name = "ULTIMATE_CREDITOR_ID")
    private Long ultimateCreditorId;

    @Column(name = "END_TO_END_ID", length = 35)
    private String endToEndId;

    @Column(name = "TRANSACTION_ID", length = 35)
    private String transactionId;

    @Column(name = "REF_ORDRE", length = 35)
    private String refOrdre;

    @Column(name = "UETR", length = 36)
    private String uetr;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 1)
    private TransferOperationStatus status;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "DATE_VALIDATION")
    private LocalDate dateValidation;

    @Lob
    @Column(name = "GARNISHMENT")
    private String garnishment;

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
    @OrderBy("partyRole ASC")
    @BatchSize(size = 50)
    @Builder.Default
    private List<TrParty> parties = new ArrayList<>();

    @OneToMany(
            mappedBy = "operation",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("accountRole ASC")
    @BatchSize(size = 50)
    @Builder.Default
    private List<TrAccount> accounts = new ArrayList<>();

    @OneToMany(
            mappedBy = "operation",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("agentRole ASC")
    @BatchSize(size = 50)
    @Builder.Default
    private List<TrFinancialAgent> financialAgents = new ArrayList<>();

    public boolean isEditable() {
        return this.status == TransferOperationStatus.X;
    }

    public void addPaymentModality(TrPaymentModality modality) {
        if (this.paymentModalities == null) {
            this.paymentModalities = new ArrayList<>();
        }

        this.paymentModalities.add(modality);
        modality.setOperation(this);
    }

    public void clearPaymentModalities() {
        if (this.paymentModalities == null) {
            this.paymentModalities = new ArrayList<>();
        } else {
            this.paymentModalities.clear();
        }
    }

    public void addParty(TrParty party) {
        if (this.parties == null) {
            this.parties = new ArrayList<>();
        }

        this.parties.add(party);
        party.setOperation(this);
    }

    public void addAccount(TrAccount account) {
        if (this.accounts == null) {
            this.accounts = new ArrayList<>();
        }

        this.accounts.add(account);
        account.setOperation(this);
    }

    public void addFinancialAgent(TrFinancialAgent agent) {
        if (this.financialAgents == null) {
            this.financialAgents = new ArrayList<>();
        }

        this.financialAgents.add(agent);
        agent.setOperation(this);
    }

    public void clearParties() {
        if (this.parties == null) {
            this.parties = new ArrayList<>();
        } else {
            this.parties.clear();
        }
    }

    public void clearAccounts() {
        if (this.accounts == null) {
            this.accounts = new ArrayList<>();
        } else {
            this.accounts.clear();
        }
    }

    public void clearFinancialAgents() {
        if (this.financialAgents == null) {
            this.financialAgents = new ArrayList<>();
        } else {
            this.financialAgents.clear();
        }
    }
}