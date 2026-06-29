package com.smi.mstr.transfer.domain.enums;

public enum PartyRole {

    /*
     * ISO 20022 customer parties
     */
    INITG_PTY("InitgPty", "Initiating party", PartyRoleFamily.CUSTOMER_PARTY),
    ULTMT_DBTR("UltmtDbtr", "Ultimate debtor", PartyRoleFamily.CUSTOMER_PARTY),
    DBTR("Dbtr", "Debtor / ordering customer", PartyRoleFamily.CUSTOMER_PARTY),
    CDTR("Cdtr", "Creditor / beneficiary", PartyRoleFamily.CUSTOMER_PARTY),
    ULTMT_CDTR("UltmtCdtr", "Ultimate creditor", PartyRoleFamily.CUSTOMER_PARTY),

    /*
     * Accounts represented inside TrParty in the compact model
     */
    DBTR_ACCT("DbtrAcct", "Debtor debit account", PartyRoleFamily.ACCOUNT),
    CDTR_ACCT("CdtrAcct", "Creditor account", PartyRoleFamily.ACCOUNT),
    CHARGES_ACCT("ChrgsAcct", "Charges / commission account", PartyRoleFamily.ACCOUNT),
    SETTLEMENT_ACCT("SttlmAcct", "Settlement account", PartyRoleFamily.ACCOUNT),
    NOSTRO_ACCT("NostroAcct", "Nostro account", PartyRoleFamily.ACCOUNT),

    /*
     * ISO 20022 financial agents
     */
    INSTG_AGT("InstgAgt", "Instructing agent", PartyRoleFamily.FINANCIAL_AGENT),
    INSTD_AGT("InstdAgt", "Instructed agent", PartyRoleFamily.FINANCIAL_AGENT),
    DBTR_AGT("DbtrAgt", "Debtor agent", PartyRoleFamily.FINANCIAL_AGENT),
    CDTR_AGT("CdtrAgt", "Creditor agent", PartyRoleFamily.FINANCIAL_AGENT),

    /*
     * Intermediary agents
     */
    INTRMY_AGT_1("IntrmyAgt1", "First intermediary agent", PartyRoleFamily.FINANCIAL_AGENT),
    INTRMY_AGT_2("IntrmyAgt2", "Second intermediary agent", PartyRoleFamily.FINANCIAL_AGENT),
    INTRMY_AGT_3("IntrmyAgt3", "Third intermediary agent", PartyRoleFamily.FINANCIAL_AGENT),

    /*
     * Cover / correspondent agents
     */
    COVER_AGT("CoverAgt", "Cover agent", PartyRoleFamily.FINANCIAL_AGENT),
    NOSTRO_AGT("NostroAgt", "Nostro agent", PartyRoleFamily.FINANCIAL_AGENT),
    REIMBURSEMENT_AGT("RmbAgt", "Reimbursement agent", PartyRoleFamily.FINANCIAL_AGENT),
    SENDER_CORRESPONDENT("SndrCorresp", "Sender correspondent", PartyRoleFamily.FINANCIAL_AGENT),
    RECEIVER_CORRESPONDENT("RcvrCorresp", "Receiver correspondent", PartyRoleFamily.FINANCIAL_AGENT),

    /*
     * Internal / operational actors
     */
    ORDERING_BRANCH("OrderingBranch", "Ordering branch", PartyRoleFamily.INTERNAL),
    PROCESSING_UNIT("ProcessingUnit", "Processing unit", PartyRoleFamily.INTERNAL);

    private final String isoName;
    private final String label;
    private final PartyRoleFamily family;

    PartyRole(String isoName, String label, PartyRoleFamily family) {
        this.isoName = isoName;
        this.label = label;
        this.family = family;
    }

    public String isoName() {
        return isoName;
    }

    public String label() {
        return label;
    }

    public PartyRoleFamily family() {
        return family;
    }

    public boolean isCustomerParty() {
        return family == PartyRoleFamily.CUSTOMER_PARTY;
    }

    public boolean isAccount() {
        return family == PartyRoleFamily.ACCOUNT;
    }

    public boolean isFinancialAgent() {
        return family == PartyRoleFamily.FINANCIAL_AGENT;
    }

    public boolean isInternal() {
        return family == PartyRoleFamily.INTERNAL;
    }
}