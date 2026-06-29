package com.smi.mstr.transfer.domain.enums;

public enum TransferOperationStatus {

    X("Draft / in progress", true, false),
    V("Validated", false, false),
    A("Applied / executed", false, true),
    R("Rejected", false, true),
    C("Cancelled", false, true),
    E("Technical or business exception", false, false);

    private final String label;
    private final boolean editable;
    private final boolean terminal;

    TransferOperationStatus(
            String label,
            boolean editable,
            boolean terminal
    ) {
        this.label = label;
        this.editable = editable;
        this.terminal = terminal;
    }

    public String label() {
        return label;
    }

    public boolean editable() {
        return editable;
    }

    public boolean terminal() {
        return terminal;
    }

    public boolean isDraft() {
        return this == X;
    }

    public boolean isValidated() {
        return this == V;
    }

    public boolean isAppliedOrExecuted() {
        return this == A;
    }

    public boolean isRejected() {
        return this == R;
    }

    public boolean isCancelled() {
        return this == C;
    }

    public boolean isException() {
        return this == E;
    }
}