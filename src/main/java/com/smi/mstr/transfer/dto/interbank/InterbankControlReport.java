package com.smi.mstr.transfer.dto.interbank;

import java.time.LocalDateTime;
import java.util.List;

public record InterbankControlReport(
        String operationRef,
        boolean valid,
        LocalDateTime controlledAt,
        List<InterbankValidationErrorDto> errors
) {
}