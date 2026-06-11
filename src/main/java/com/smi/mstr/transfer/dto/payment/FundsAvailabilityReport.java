package com.smi.mstr.transfer.dto.payment;

import com.smi.mstr.transfer.domain.enums.FundsCheckStatus;

import java.time.LocalDateTime;
import java.util.List;

public record FundsAvailabilityReport(
        String operationRef,
        FundsCheckStatus overallStatus,
        LocalDateTime checkedAt,
        List<FundsAvailabilityItemDto> results
) {}