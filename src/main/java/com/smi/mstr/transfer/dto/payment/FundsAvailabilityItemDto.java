package com.smi.mstr.transfer.dto.payment;

import com.smi.mstr.transfer.domain.enums.FundsCheckStatus;

import java.math.BigDecimal;

public record FundsAvailabilityItemDto(
        Long modalityId,
        String debitAccountRef,
        BigDecimal requestedAmount,
        String requestedCurrency,
        BigDecimal availableAmount,
        FundsCheckStatus status,
        String message
) {}