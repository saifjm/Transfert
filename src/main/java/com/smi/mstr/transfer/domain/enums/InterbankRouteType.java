package com.smi.mstr.transfer.domain.enums;

public enum InterbankRouteType {

    DIRECT_NOSTRO(
            "Direct Nostro/Vostro relationship",
            false,
            false
    ),

    SERIAL_INTERMEDIARY(
            "Serial intermediary route",
            false,
            true
    ),

    COVER_REQUIRED(
            "Separate cover route required",
            true,
            false
    );

    private final String label;
    private final boolean coverRequired;
    private final boolean intermediaryRoute;

    InterbankRouteType(
            String label,
            boolean coverRequired,
            boolean intermediaryRoute
    ) {
        this.label = label;
        this.coverRequired = coverRequired;
        this.intermediaryRoute = intermediaryRoute;
    }

    public String label() {
        return label;
    }

    public boolean coverRequired() {
        return coverRequired;
    }

    public boolean intermediaryRoute() {
        return intermediaryRoute;
    }

    public boolean isDirectNostro() {
        return this == DIRECT_NOSTRO;
    }

    public boolean isSerialIntermediary() {
        return this == SERIAL_INTERMEDIARY;
    }

    public boolean isCoverRequired() {
        return this == COVER_REQUIRED;
    }
}