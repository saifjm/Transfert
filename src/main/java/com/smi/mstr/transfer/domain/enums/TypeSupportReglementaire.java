package com.smi.mstr.transfer.domain.enums;

public enum TypeSupportReglementaire {

    TCE(
            "Titre de commerce extérieur",
            true,
            true,
            true
    ),

    FICHE_INFORMATION(
            "Fiche d'information",
            true,
            true,
            true
    ),

    AUTORISATION_BCT(
            "Autorisation BCT",
            true,
            false,
            true
    ),

    DEROGATION_BCT(
            "Dérogation BCT",
            true,
            false,
            true
    ),

    CONTRAT_COMMERCIAL(
            "Contrat commercial",
            false,
            false,
            true
    ),

    FACTURE(
            "Facture commerciale",
            false,
            false,
            false
    ),

    DOCUMENT_TRANSPORT(
            "Document de transport",
            false,
            false,
            false
    ),

    JUSTIFICATIF_PAIEMENT(
            "Justificatif de paiement",
            false,
            false,
            false
    ),

    DECLARATION_DOUANIERE(
            "Déclaration douanière",
            false,
            false,
            true
    ),

    AUTRE(
            "Autre support réglementaire",
            false,
            false,
            false
    );

    private final String label;
    private final boolean validationRequired;
    private final boolean reservationRequired;
    private final boolean regulatoryReportingRelevant;

    TypeSupportReglementaire(
            String label,
            boolean validationRequired,
            boolean reservationRequired,
            boolean regulatoryReportingRelevant
    ) {
        this.label = label;
        this.validationRequired = validationRequired;
        this.reservationRequired = reservationRequired;
        this.regulatoryReportingRelevant = regulatoryReportingRelevant;
    }

    public String label() {
        return label;
    }

    public boolean validationRequired() {
        return validationRequired;
    }

    public boolean reservationRequired() {
        return reservationRequired;
    }

    public boolean regulatoryReportingRelevant() {
        return regulatoryReportingRelevant;
    }

    public boolean isTce() {
        return this == TCE;
    }

    public boolean isFicheInformation() {
        return this == FICHE_INFORMATION;
    }

    public boolean isAutorisationBct() {
        return this == AUTORISATION_BCT || this == DEROGATION_BCT;
    }
}