package com.smi.mstr.transfer.domain.enums;

public enum OriginChannel {

    AGENCY("Agency / branch front-office"),
    FRONT_OFFICE("Front-office channel"),
    BACK_OFFICE("Back-office channel"),
    API("External or internal API"),
    BATCH("Batch import"),
    PORTAL("Client portal"),
    SWIFT_PLUS("SWIFT+ Messaging Hub"),
    INTERBANK("Interbank channel"),
    SYSTEM("System-generated operation"),
    MIGRATION("Migrated legacy operation");

    private final String label;

    OriginChannel(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static OriginChannel from(String value) {
        if (value == null || value.isBlank()) {
            return AGENCY;
        }

        String normalized = value.trim().toUpperCase();

        return switch (normalized) {
            case "AGENCE", "BRANCH" -> AGENCY;
            case "FO", "FRONT" -> FRONT_OFFICE;
            case "BO", "BACK" -> BACK_OFFICE;
            case "SWIFT", "SWIFT+" -> SWIFT_PLUS;
            default -> OriginChannel.valueOf(normalized);
        };
    }
}