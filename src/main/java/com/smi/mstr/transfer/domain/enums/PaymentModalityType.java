package com.smi.mstr.transfer.domain.enums;

public enum PaymentModalityType {

    /**
     * Client paie en TND ; banque achète la devise au cours normal.
     * Exemple : compte TND -> transfert EUR.
     */
    TND_FX_PURCHASE_NORMAL,

    /**
     * Client paie en TND ; banque applique un cours négocié.
     */
    TND_FX_PURCHASE_NEGOTIATED,

    /**
     * Utilisation d'un contrat de change à terme.
     */
    FORWARD_FX_CONTRACT,

    /**
     * Débit direct d'un compte devise dans la même devise que le transfert.
     */
    DIRECT_FOREIGN_CURRENCY_ACCOUNT_DEBIT,

    /**
     * Débit d'un compte devise différente de la devise du transfert.
     * Exemple : compte USD -> transfert EUR.
     */
    CURRENCY_ARBITRAGE,

    /**
     * Fonds reçus d'une autre banque locale.
     */
    FUNDS_RECEIVED_LOCAL_BANK,

    /**
     * Couverture ou négociation interbancaire.
     */
    INTERBANK_FX_COVER,

    /**
     * Financement import / ligne de financement en devise.
     */
    IMPORT_FINANCING,

    OTHER
}