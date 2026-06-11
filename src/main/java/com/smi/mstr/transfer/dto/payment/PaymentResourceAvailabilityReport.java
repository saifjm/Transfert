package com.smi.mstr.transfer.dto.payment;

import com.smi.mstr.transfer.domain.enums.PaymentResourceAvailabilityStatus;

import java.time.LocalDateTime;
import java.util.List;

public record PaymentResourceAvailabilityReport(
        String operationRef,
        PaymentResourceAvailabilityStatus overallStatus,
        LocalDateTime checkedAt,
        List<PaymentResourceAvailabilityItemDto> results
) {}