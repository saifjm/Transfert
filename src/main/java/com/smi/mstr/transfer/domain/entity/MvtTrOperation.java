package com.smi.mstr.transfer.domain.entity;

import com.smi.mstr.transfer.domain.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "MVT_TR_OPERATION",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_MVT_TR_OPERATION_REF", columnNames = "OPERATION_REF")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MvtTrOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mvt_tr_operation_seq")
    @SequenceGenerator(
            name = "mvt_tr_operation_seq",
            sequenceName = "SEQ_MVT_TR_OPERATION",
            allocationSize = 1
    )
    @Column(name = "REF_OPERATION")
    private Long refOperation;

    @Column(name = "OPERATION_REF", nullable = false, length = 35)
    private String operationRef;

    @Column(name = "CORRELATION_ID", length = 100)
    private String correlationId;

    @Column(name = "UETR", length = 36)
    private String uetr;

    @Column(name = "END_TO_END_ID", length = 35)
    private String endToEndId;

    @Column(name = "INSTRUCTION_ID", length = 35)
    private String instructionId;

    @Column(name = "TRANSACTION_ID", length = 35)
    private String transactionId;

    @Column(name = "DATE_OPERATION", nullable = false)
    private LocalDate dateOperation;

    @Column(name = "DATE_VALIDATION")
    private LocalDate dateValidation;

    @Column(name = "SETTLEMENT_DATE")
    private LocalDate settlementDate;

    @Column(name = "REQUESTED_EXECUTION_DATE")
    private LocalDate requestedExecutionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 1)
    private TransferOperationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE_TRANSFERT", nullable = false, length = 1)
    private TransferType transferType;

    @Enumerated(EnumType.STRING)
    @Column(name = "SWIFT_PRIORITY", length = 1)
    private SwiftPriority swiftPriority;

    @Column(name = "SERVICE_LEVEL_CODE", length = 10)
    private String serviceLevelCode;

    @Column(name = "LOCAL_INSTRUMENT_CODE", length = 35)
    private String localInstrumentCode;

    @Column(name = "CATEGORY_PURPOSE_CODE", length = 35)
    private String categoryPurposeCode;

    @Column(name = "NUM_DOSSIER", length = 35)
    private String numDossier;

    @Column(name = "DATE_DOSSIER")
    private LocalDate dateDossier;

    @Column(name = "BRANCH_CODE", length = 10)
    private String branchCode;

    @Column(name = "CREATED_BY", length = 50)
    private String createdBy;

    @Column(name = "VALIDATED_BY", length = 50)
    private String validatedBy;

    @Column(name = "MNT_ORDRE", precision = 18, scale = 3)
    private BigDecimal orderAmount;

    @Column(name = "CCY_ORDRE", length = 3)
    private String orderCurrency;

    @Column(name = "CODE_DEVISE_ORDRE_BCT")
    private Integer orderCurrencyBctCode;

    @Column(name = "MNT_TRANSFERT", precision = 18, scale = 3)
    private BigDecimal transferAmount;

    @Column(name = "CCY_TRANSFERT", length = 3)
    private String transferCurrency;

    @Column(name = "CODE_DEVISE_TRANSFERT_BCT")
    private Integer transferCurrencyBctCode;

    @Column(name = "CONTRE_VALEUR_TND", precision = 18, scale = 3)
    private BigDecimal counterValueTnd;

    @Column(name = "FX_RATE", precision = 18, scale = 8)
    private BigDecimal fxRate;

    @Column(name = "FX_DEAL_REF", length = 35)
    private String fxDealRef;

    @Column(name = "PURPOSE_CODE", length = 35)
    private String purposeCode;

    @Column(name = "PURPOSE_PROPRIETARY", length = 140)
    private String purposeProprietary;

    @Column(name = "REMITTANCE_UNSTRUCTURED", length = 560)
    private String remittanceUnstructured;

    @Column(name = "CHARGE_BEARER", length = 4)
    private String chargeBearer;

    @Column(name = "CHARGES_ACCOUNT_REF", length = 100)
    private String chargesAccountRef;

    @Column(name = "WORKFLOW_INSTANCE_ID", length = 100)
    private String workflowInstanceId;

    @Column(name = "WORKFLOW_TASK_ID", length = 100)
    private String workflowTaskId;

    @Lob
    @Column(name = "WORKFLOW_CONTEXT_JSON")
    private String workflowContextJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "SOURCE_CHANNEL", length = 30)
    private OriginChannel sourceChannel;

    @Column(name = "SOURCE_MODULE", length = 50)
    private String sourceModule;

    @Column(name = "SOURCE_REFERENCE", length = 100)
    private String sourceReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "COMPLETION_STATUS", length = 20)
    private CompletionStatus completionStatus;

    @OneToMany(
            mappedBy = "operation",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<TrParty> parties = new ArrayList<>();

    @OneToMany(
            mappedBy = "operation",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<TrAccount> accounts = new ArrayList<>();

    @OneToMany(
            mappedBy = "operation",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<TrFinancialAgent> financialAgents = new ArrayList<>();

    @OneToMany(
            mappedBy = "operation",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("sequenceNo ASC")
    @BatchSize(size = 50)
    @Builder.Default
    private List<TrPaymentModality> paymentModalities = new ArrayList<>();

    @Lob
    @Column(name = "RAW_INPUT_SNAPSHOT")
    private String rawInputSnapshot;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Long version;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    public boolean isEditable() {
        return this.status == TransferOperationStatus.X;
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

    public void addFinancialAgent(TrFinancialAgent agent) {
        if (this.financialAgents == null) {
            this.financialAgents = new ArrayList<>();
        }
        this.financialAgents.add(agent);
        agent.setOperation(this);
    }

    public void clearNormalizedOrderData() {
        if (this.accounts == null) {
            this.accounts = new ArrayList<>();
        } else {
            this.accounts.clear();
        }

        if (this.financialAgents == null) {
            this.financialAgents = new ArrayList<>();
        } else {
            this.financialAgents.clear();
        }

        if (this.parties == null) {
            this.parties = new ArrayList<>();
        } else {
            this.parties.clear();
        }
    }

    public void removeAccountsByRole(AccountRole role) {
        if (this.accounts != null) {
            this.accounts.removeIf(account -> account.getAccountRole() == role);
        }
    }

    public void removePartiesByRole(PartyRole role) {
        if (this.parties != null) {
            this.parties.removeIf(party -> party.getPartyRole() == role);
        }
    }

    public void removeFinancialAgentsByRole(FinancialAgentRole role) {
        if (this.financialAgents != null) {
            this.financialAgents.removeIf(agent -> agent.getAgentRole() == role);
        }
    }

}