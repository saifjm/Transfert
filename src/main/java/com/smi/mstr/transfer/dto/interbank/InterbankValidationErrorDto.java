package com.smi.mstr.transfer.dto.interbank;

import com.smi.mstr.transfer.domain.enums.ValidationSeverity;

public record InterbankValidationErrorDto(
        String fieldPath,
        String errorCode,
        String errorMessage,
        ValidationSeverity severity
) {
}