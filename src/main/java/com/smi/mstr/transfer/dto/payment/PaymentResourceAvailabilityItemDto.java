package com.smi.mstr.transfer.dto.payment;

import com.smi.mstr.transfer.domain.enums.PaymentResourceAvailabilityStatus;
import com.smi.mstr.transfer.domain.enums.PaymentResourceType;

import java.math.BigDecimal;

public record PaymentResourceAvailabilityItemDto(
        Long modalityId,
        PaymentResourceType resourceType,
        String resourceRef,

        BigDecimal requestedAmount,
        String requestedCurrency,

        BigDecimal availableAmount,
        String availableCurrency,

        PaymentResourceAvailabilityStatus status,
        String message
) {}