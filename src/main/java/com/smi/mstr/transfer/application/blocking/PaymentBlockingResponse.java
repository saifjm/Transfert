package com.smi.mstr.transfer.application.blocking;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentBlockingResponse(

        PaymentBlockingResultStatus status,

        String blockingReference,

        BigDecimal blockedAmount,

        String blockedCurrency,

        String message,

        String rawPayload,

        LocalDateTime blockedAt
) {

    public boolean success() {
        return status == PaymentBlockingResultStatus.BLOCKED
                || status == PaymentBlockingResultStatus.PARTIALLY_BLOCKED
                || status == PaymentBlockingResultStatus.NOT_REQUIRED;
    }

    public boolean fullyBlocked() {
        return status == PaymentBlockingResultStatus.BLOCKED
                || status == PaymentBlockingResultStatus.NOT_REQUIRED;
    }

    public boolean failed() {
        return !success();
    }
}