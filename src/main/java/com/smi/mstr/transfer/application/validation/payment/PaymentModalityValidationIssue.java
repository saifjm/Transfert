package com.smi.mstr.transfer.application.validation.payment;

import com.smi.mstr.transfer.domain.enums.PaymentModalityType;

public record PaymentModalityValidationIssue(
        PaymentModalityValidationSeverity severity,
        String code,
        String field,
        String message,
        Integer sequenceNo,
        PaymentModalityType modalityType
) {
}
