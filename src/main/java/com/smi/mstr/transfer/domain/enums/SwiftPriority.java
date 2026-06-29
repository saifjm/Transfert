package com.smi.mstr.transfer.domain.enums;

public enum SwiftPriority {

    N("Normal priority"),
    U("Urgent priority"),
    S("System priority");

    private final String label;

    SwiftPriority(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public boolean isUrgent() {
        return this == U;
    }

    public boolean isNormal() {
        return this == N;
    }

    public static SwiftPriority from(String value) {
        if (value == null || value.isBlank()) {
            return N;
        }

        String normalized = value.trim().toUpperCase();

        return switch (normalized) {
            case "N", "NORMAL" -> N;
            case "U", "URGENT", "HIGH" -> U;
            case "S", "SYSTEM" -> S;
            default -> throw new IllegalArgumentException(
                    "Invalid SWIFT priority: " + value
            );
        };
    }
}