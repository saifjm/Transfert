package com.smi.mstr.transfer.dto.payment;

import com.smi.mstr.transfer.domain.enums.ValidationSeverity;

public record PaymentValidationAnomalyDto(
        ValidationSeverity severity,
        String anomalyCode,
        Long modalityId,
        String modalityType,
        String fieldPath,
        String message
) {}