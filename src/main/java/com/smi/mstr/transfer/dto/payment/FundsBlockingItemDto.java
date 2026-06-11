package com.smi.mstr.transfer.dto.payment;

import com.smi.mstr.transfer.domain.enums.FundsBlockingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FundsBlockingItemDto(
        Long modalityId,
        String debitAccountRef,

        boolean blockingRequired,

        BigDecimal requestedAmount,
        String requestedCurrency,

        BigDecimal blockedAmount,
        String blockedCurrency,
        String blockingReference,

        FundsBlockingStatus status,
        String message
) {}