package com.smi.mstr.transfer.domain.entity;

import com.smi.mstr.transfer.domain.enums.PartyRole;
import com.smi.mstr.transfer.domain.enums.PartyType;
import com.smi.mstr.transfer.domain.enums.ResidencyStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "TR_PARTY",
        schema = "TRSF",
        indexes = {
                @Index(name = "IX_TR_PARTY_OPERATION", columnList = "REF_OPERATION"),
                @Index(name = "IX_TR_PARTY_ROLE", columnList = "REF_OPERATION, PARTY_ROLE"),
                @Index(name = "IX_TR_PARTY_BIC", columnList = "BIC"),
                @Index(name = "IX_TR_PARTY_ACCOUNT_IBAN", columnList = "ACCOUNT_IBAN"),
                @Index(name = "IX_TR_PARTY_CUSTOMER", columnList = "CUSTOMER_ID")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrParty {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tr_party_seq")
    @SequenceGenerator(
            name = "tr_party_seq",
            sequenceName = "TRSF.SEQ_TR_PARTY",
            allocationSize = 1
    )
    @Column(name = "ID_PARTY", nullable = false)
    private Long idParty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REF_OPERATION", nullable = false)
    private MvtTrOperation operation;

    /*
     * Display / processing order inside the operation.
     *
     * Required because MvtTrOperation uses:
     * @OrderBy("partyRole ASC, sequenceNo ASC")
     */
    @Column(name = "SEQUENCE_NO", nullable = false)
    private Integer sequenceNo;

    /*
     * Role of the row inside the transfer aggregate.
     *
     * Examples:
     * DBTR, CDTR, ULTMT_DBTR, CDTR_ACCT, CHARGES_ACCT,
     * DBTR_AGT, CDTR_AGT, INTRMY_AGT_1, COVER_AGT, etc.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "PARTY_ROLE", nullable = false, length = 40)
    private PartyRole partyRole;

    /*
     * Logical nature of the party row.
     *
     * For the compact model:
     * - customer parties: PERSON / ORG
     * - accounts: ACCOUNT
     * - banks / agents: BANK
     * - internal actors: INTERNAL
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "PARTY_TYPE", nullable = false, length = 20)
    private PartyType partyType;

    /*
     * -------------------------------------------------------------------------
     * 1. Referential / customer identity
     * -------------------------------------------------------------------------
     */

    @Column(name = "CUSTOMER_ID")
    private Long customerId;

    @Column(name = "EXTERNAL_PARTY_REF", length = 100)
    private String externalPartyRef;

    @Column(name = "NAME", length = 140)
    private String name;

    @Column(name = "COUNTRY_CODE", length = 3)
    private String countryCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "RESIDENCY_STATUS", length = 20)
    private ResidencyStatus residencyStatus;

    /*
     * -------------------------------------------------------------------------
     * 2. Identification
     * -------------------------------------------------------------------------
     */

    @Column(name = "IDENTIFICATION_TYPE", length = 30)
    private String identificationType;

    @Column(name = "IDENTIFICATION_VALUE", length = 50)
    private String identificationValue;

    @Column(name = "IDENTIFICATION_ISSUER", length = 50)
    private String identificationIssuer;

    @Column(name = "IDENTIFICATION_SCHEME", length = 35)
    private String identificationScheme;

    @Column(name = "LEI", length = 20)
    private String lei;

    /*
     * -------------------------------------------------------------------------
     * 3. Postal address
     * -------------------------------------------------------------------------
     */

    @Column(name = "ADDRESS_LINE1", length = 140)
    private String addressLine1;

    @Column(name = "ADDRESS_LINE2", length = 140)
    private String addressLine2;

    @Column(name = "ADDRESS_LINE3", length = 140)
    private String addressLine3;

    @Column(name = "TOWN_NAME", length = 70)
    private String townName;

    @Column(name = "POST_CODE", length = 20)
    private String postCode;

    @Column(name = "COUNTRY_SUB_DIVISION", length = 70)
    private String countrySubDivision;

    /*
     * -------------------------------------------------------------------------
     * 4. Account data
     *
     * Used when PARTY_ROLE is:
     * - DBTR_ACCT
     * - CDTR_ACCT
     * - CHARGES_ACCT
     * - SETTLEMENT_ACCT
     * - NOSTRO_ACCT
     * -------------------------------------------------------------------------
     */

    @Column(name = "ACCOUNT_NUMBER", length = 34)
    private String accountNumber;

    @Column(name = "ACCOUNT_IBAN", length = 34)
    private String accountIban;

    @Column(name = "ACCOUNT_SCHEME", length = 20)
    private String accountScheme;

    @Column(name = "ACCOUNT_CURRENCY", length = 3)
    private String accountCurrency;

    @Column(name = "ACCOUNT_TYPE", length = 30)
    private String accountType;

    @Column(name = "ACCOUNT_NAME", length = 140)
    private String accountName;

    /*
     * -------------------------------------------------------------------------
     * 5. Bank / financial agent data
     *
     * Used when PARTY_ROLE is:
     * - DBTR_AGT
     * - CDTR_AGT
     * - INSTG_AGT
     * - INSTD_AGT
     * - INTRMY_AGT_1 / 2 / 3
     * - COVER_AGT
     * - NOSTRO_AGT
     * - REIMBURSEMENT_AGT
     * -------------------------------------------------------------------------
     */

    @Column(name = "BIC", length = 11)
    private String bic;

    @Column(name = "BANK_CODE", length = 30)
    private String bankCode;

    @Column(name = "BANK_NAME", length = 140)
    private String bankName;

    @Column(name = "BANK_BRANCH_CODE", length = 30)
    private String bankBranchCode;

    @Column(name = "BANK_BRANCH_NAME", length = 140)
    private String bankBranchName;

    @Column(name = "BANK_COUNTRY_CODE", length = 3)
    private String bankCountryCode;

    @Column(name = "CLEARING_SYSTEM_CODE", length = 20)
    private String clearingSystemCode;

    @Column(name = "CLEARING_MEMBER_ID", length = 35)
    private String clearingMemberId;

    /*
     * Position in the payment route.
     *
     * Examples:
     * 1 = debtor agent
     * 2 = intermediary 1
     * 3 = intermediary 2
     * 4 = creditor agent
     */
    @Column(name = "AGENT_POSITION")
    private Integer agentPosition;

    @Column(name = "ROUTING_ROLE", length = 40)
    private String routingRole;

    /*
     * -------------------------------------------------------------------------
     * 6. Source / audit snapshot
     * -------------------------------------------------------------------------
     */

    @Column(name = "SOURCE_SYSTEM", length = 50)
    private String sourceSystem;

    @Column(name = "SOURCE_REFERENCE", length = 100)
    private String sourceReference;

    @Lob
    @Column(name = "PARTY_SNAPSHOT_JSON")
    private String partySnapshotJson;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    /*
     * -------------------------------------------------------------------------
     * JPA lifecycle
     * -------------------------------------------------------------------------
     */

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (sequenceNo == null) {
            sequenceNo = 1;
        }

        if (partyType == null) {
            partyType = resolveDefaultPartyType();
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
     * Domain helpers
     * -------------------------------------------------------------------------
     */

    public boolean isCustomerParty() {
        return partyRole != null && partyRole.isCustomerParty();
    }

    public boolean isAccount() {
        return partyRole != null && partyRole.isAccount();
    }

    public boolean isFinancialAgent() {
        return partyRole != null && partyRole.isFinancialAgent();
    }

    public boolean isInternalParty() {
        return partyRole != null && partyRole.isInternal();
    }

    public boolean hasAccountIdentifier() {
        return notBlank(accountIban)
                || notBlank(accountNumber)
                || notBlank(externalPartyRef);
    }

    public boolean hasBankIdentifier() {
        return notBlank(bic)
                || notBlank(bankCode)
                || notBlank(clearingMemberId);
    }

    public String resolveAccountReference() {
        if (notBlank(accountIban)) {
            return accountIban;
        }

        if (notBlank(accountNumber)) {
            return accountNumber;
        }

        if (notBlank(externalPartyRef)) {
            return externalPartyRef;
        }

        return null;
    }

    public String resolveBankReference() {
        if (notBlank(bic)) {
            return bic;
        }

        if (notBlank(bankCode)) {
            return bankCode;
        }

        if (notBlank(clearingMemberId)) {
            return clearingMemberId;
        }

        return null;
    }

    private PartyType resolveDefaultPartyType() {
        if (partyRole == null) {
            return PartyType.OTHER;
        }

        if (partyRole.isAccount()) {
            return PartyType.ACCOUNT;
        }

        if (partyRole.isFinancialAgent()) {
            return PartyType.BANK;
        }

        if (partyRole.isInternal()) {
            return PartyType.INTERNAL;
        }

        return PartyType.ORG;
    }

    private void normalizeCodes() {
        countryCode = upper(countryCode);
        bankCountryCode = upper(bankCountryCode);
        accountCurrency = upper(accountCurrency);
        bic = upper(bic);
        lei = upper(lei);
        clearingSystemCode = upper(clearingSystemCode);
        identificationType = upper(identificationType);
        identificationIssuer = upper(identificationIssuer);
        accountScheme = upper(accountScheme);
        accountType = upper(accountType);
        routingRole = upper(routingRole);
        sourceSystem = upper(sourceSystem);
    }

    private String upper(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}