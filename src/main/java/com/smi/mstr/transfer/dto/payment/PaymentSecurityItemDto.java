package com.smi.mstr.transfer.dto.payment;

import com.smi.mstr.transfer.domain.enums.PaymentResourceType;
import com.smi.mstr.transfer.domain.enums.PaymentSecurityStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentSecurityItemDto(
        Long securityId,
        Long modalityId,

        PaymentResourceType resourceType,
        PaymentSecurityStatus securityStatus,
        String resourceRef,

        BigDecimal requestedAmount,
        String requestedCurrency,

        BigDecimal fxRate,
        BigDecimal counterValueAmount,
        String counterValueCurrency,

        BigDecimal estimatedFeesAmount,
        String estimatedFeesCurrency,

        BigDecimal securedAmount,
        String securedCurrency,

        String securityReference,
        LocalDateTime securedAt,
        String message
) {}