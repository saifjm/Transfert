package com.smi.mstr.transfer.application.payment;

import com.smi.mstr.transfer.domain.enums.PaymentResourceType;

import java.math.BigDecimal;

public record PaymentSecurityCommand(
        Long modalityId,
        PaymentResourceType resourceType,
        String resourceRef,

        BigDecimal requestedAmount,
        String requestedCurrency,

        BigDecimal fxRate,
        BigDecimal counterValueAmount,
        String counterValueCurrency,

        BigDecimal estimatedFeesAmount,
        String estimatedFeesCurrency,

        BigDecimal amountToSecure,
        String currencyToSecure
) {}
