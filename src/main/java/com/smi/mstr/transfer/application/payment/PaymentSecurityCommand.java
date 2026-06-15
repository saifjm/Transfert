package com.smi.mstr.transfer.application.payment;

import com.smi.mstr.transfer.domain.enums.PaymentImpactAction;
import com.smi.mstr.transfer.domain.enums.PaymentImpactTarget;
import com.smi.mstr.transfer.domain.enums.PaymentResourceType;

import java.math.BigDecimal;

public record PaymentSecurityCommand(
        Long modalityId,

        PaymentResourceType resourceType,
        PaymentImpactTarget impactTarget,
        PaymentImpactAction impactAction,

        String resourceRef,

        BigDecimal requestedTransferAmount,
        String requestedTransferCurrency,

        BigDecimal fxRate,
        BigDecimal counterValueAmount,
        String counterValueCurrency,

        BigDecimal estimatedFeesAmount,
        String estimatedFeesCurrency,

        BigDecimal amountToSecure,
        String currencyToSecure
) {}