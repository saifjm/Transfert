package com.smi.mstr.transfer.domain.enums;

public enum PaymentModalityType {

    TND_FX_PURCHASE_NORMAL(
            "Achat devise contre TND - cours normal",
            true,
            true,
            PaymentResourceType.ACCOUNT
    ),

    NEGOTIATED_FX_PURCHASE(
            "Achat devise contre TND - cours négocié",
            true,
            true,
            PaymentResourceType.FX_DEAL
    ),

    FORWARD_FX(
            "Achat devise à terme",
            true,
            true,
            PaymentResourceType.FX_CONTRACT
    ),

    DIRECT_FOREIGN_CURRENCY_ACCOUNT_DEBIT(
            "Débit direct compte devise",
            false,
            true,
            PaymentResourceType.ACCOUNT
    ),

    ARBITRAGE(
            "Arbitrage devise contre devise",
            true,
            true,
            PaymentResourceType.FX_DEAL
    ),

    FUNDS_RECEIVED_FROM_LOCAL_BANK(
            "Fonds reçus d'une autre banque locale",
            false,
            true,
            PaymentResourceType.RECEIVED_FUNDS
    ),

    INTERBANK_NEGOTIATION(
            "Négociation devise auprès d'une autre banque",
            true,
            true,
            PaymentResourceType.INTERBANK_DEAL
    ),

    INTERBANK_COVER(
            "Couverture interbancaire",
            false,
            true,
            PaymentResourceType.INTERBANK_COVER
    ),

    IMPORT_FINANCING(
            "Financement import / dossier de financement devise",
            false,
            true,
            PaymentResourceType.FINANCING_FILE
    ),

    MANUAL_EXCEPTION(
            "Modalité exceptionnelle manuelle",
            false,
            false,
            PaymentResourceType.OTHER
    );

    private final String label;
    private final boolean fxRequiredByDefault;
    private final boolean blockingRequiredByDefault;
    private final PaymentResourceType defaultResourceType;

    PaymentModalityType(
            String label,
            boolean fxRequiredByDefault,
            boolean blockingRequiredByDefault,
            PaymentResourceType defaultResourceType
    ) {
        this.label = label;
        this.fxRequiredByDefault = fxRequiredByDefault;
        this.blockingRequiredByDefault = blockingRequiredByDefault;
        this.defaultResourceType = defaultResourceType;
    }

    public String label() {
        return label;
    }

    public boolean fxRequiredByDefault() {
        return fxRequiredByDefault;
    }

    public boolean blockingRequiredByDefault() {
        return blockingRequiredByDefault;
    }

    public PaymentResourceType defaultResourceType() {
        return defaultResourceType;
    }

    public boolean isAccountBased() {
        return defaultResourceType == PaymentResourceType.ACCOUNT;
    }

    public boolean isFinancingBased() {
        return defaultResourceType == PaymentResourceType.FINANCING_FILE;
    }

    public boolean isInterbankBased() {
        return defaultResourceType == PaymentResourceType.INTERBANK_DEAL
                || defaultResourceType == PaymentResourceType.INTERBANK_COVER;
    }
}