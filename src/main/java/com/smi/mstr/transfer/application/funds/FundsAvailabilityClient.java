package com.smi.mstr.transfer.application.funds;

import com.smi.mstr.transfer.dto.payment.FundsAvailabilityItemDto;

import java.math.BigDecimal;

public interface FundsAvailabilityClient {

    FundsAvailabilityItemDto checkAvailability(
            Long modalityId,
            String debitAccountRef,
            BigDecimal requestedAmount,
            String requestedCurrency
    );
}