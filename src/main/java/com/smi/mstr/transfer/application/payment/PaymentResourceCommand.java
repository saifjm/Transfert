package com.smi.mstr.transfer.application.payment;

import com.smi.mstr.transfer.domain.enums.PaymentImpactAction;
import com.smi.mstr.transfer.domain.enums.PaymentImpactTarget;
import com.smi.mstr.transfer.domain.enums.PaymentResourceType;

import java.math.BigDecimal;

public record PaymentResourceCommand(
        Long modalityId,

        PaymentResourceType resourceType,
        PaymentImpactTarget impactTarget,
        PaymentImpactAction impactAction,

        String resourceRef,

        BigDecimal amountToCheck,
        String currencyToCheck,

        BigDecimal requestedTransferAmount,
        String requestedTransferCurrency,

        BigDecimal fxRate,
        BigDecimal counterValueAmount,
        String counterValueCurrency
) {}