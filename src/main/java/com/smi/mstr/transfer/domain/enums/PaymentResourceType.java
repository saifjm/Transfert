package com.smi.mstr.transfer.domain.enums;

public enum PaymentResourceType {

    ACCOUNT(
            "Account-based resource",
            true,
            false,
            false,
            false
    ),

    FX_DEAL(
            "Spot or negotiated FX deal",
            false,
            true,
            false,
            false
    ),

    FX_CONTRACT(
            "Forward FX contract",
            false,
            true,
            false,
            false
    ),

    FINANCING_FILE(
            "Import financing file",
            false,
            false,
            true,
            false
    ),

    RECEIVED_FUNDS(
            "Funds received from local bank",
            false,
            false,
            false,
            false
    ),

    INTERBANK_DEAL(
            "Interbank currency negotiation",
            false,
            true,
            false,
            true
    ),

    INTERBANK_COVER(
            "Interbank cover resource",
            false,
            false,
            false,
            true
    ),

    OTHER(
            "Other resource",
            false,
            false,
            false,
            false
    );

    private final String label;
    private final boolean accountBased;
    private final boolean fxBased;
    private final boolean financingBased;
    private final boolean interbankBased;

    PaymentResourceType(
            String label,
            boolean accountBased,
            boolean fxBased,
            boolean financingBased,
            boolean interbankBased
    ) {
        this.label = label;
        this.accountBased = accountBased;
        this.fxBased = fxBased;
        this.financingBased = financingBased;
        this.interbankBased = interbankBased;
    }

    public String label() {
        return label;
    }

    public boolean isAccountBased() {
        return accountBased;
    }

    public boolean isFxBased() {
        return fxBased;
    }

    public boolean isFinancingBased() {
        return financingBased;
    }

    public boolean isInterbankBased() {
        return interbankBased;
    }

    public boolean isReceivedFundsBased() {
        return this == RECEIVED_FUNDS;
    }

    public boolean isInterbankCoverBased() {
        return this == INTERBANK_COVER;
    }

    public boolean isInterbankDealBased() {
        return this == INTERBANK_DEAL;
    }
}