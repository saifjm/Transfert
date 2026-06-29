package com.smi.mstr.transfer.application.blocking;

public enum PaymentBlockingResultStatus {
    BLOCKED,
    PARTIALLY_BLOCKED,
    NOT_REQUIRED,
    INSUFFICIENT_FUNDS,
    FAILED
}
