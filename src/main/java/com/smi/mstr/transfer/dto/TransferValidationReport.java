package com.smi.mstr.transfer.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TransferValidationReport(
        String operationRef,
        boolean validForSubmission,
        LocalDateTime controlledAt,
        List<ValidationErrorDto> errors
) {}