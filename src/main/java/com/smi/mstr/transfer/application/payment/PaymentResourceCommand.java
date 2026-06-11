package com.smi.mstr.transfer.application.payment;

import com.smi.mstr.transfer.domain.enums.PaymentResourceType;

import java.math.BigDecimal;

public record PaymentResourceCommand(
        Long modalityId,
        PaymentResourceType resourceType,
        String resourceRef,

        BigDecimal amountToCheck,
        String currencyToCheck,

        BigDecimal requestedAmount,
        String requestedCurrency
) {}