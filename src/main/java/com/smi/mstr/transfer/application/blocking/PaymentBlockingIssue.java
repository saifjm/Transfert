package com.smi.mstr.transfer.application.blocking;

import com.smi.mstr.transfer.domain.enums.PaymentModalityType;

public record PaymentBlockingIssue(
        String code,
        String field,
        String message,
        Integer sequenceNo,
        PaymentModalityType modalityType
) {
}
