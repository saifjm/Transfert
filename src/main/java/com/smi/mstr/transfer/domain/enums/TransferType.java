package com.smi.mstr.transfer.domain.enums;

public enum TransferType {

    C("Commercial transfer"),
    F("Financial transfer");

    private final String label;

    TransferType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public boolean isCommercial() {
        return this == C;
    }

    public boolean isFinancial() {
        return this == F;
    }

    public static TransferType from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Transfer type is required.");
        }

        String normalized = value.trim().toUpperCase();

        return switch (normalized) {
            case "C", "COMMERCIAL", "TRC" -> C;
            case "F", "FINANCIAL", "FINANCIER" -> F;
            default -> throw new IllegalArgumentException(
                    "Invalid transfer type: " + value
            );
        };
    }
}