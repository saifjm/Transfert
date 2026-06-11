package com.smi.mstr.transfer.dto;

import com.smi.mstr.transfer.domain.enums.ValidationSection;
import com.smi.mstr.transfer.domain.enums.ValidationSeverity;

public record ValidationErrorDto(
        ValidationSection section,
        String fieldPath,
        String errorCode,
        String errorMessage,
        ValidationSeverity severity
) {}