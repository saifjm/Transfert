package com.smi.mstr.transfer.application.funds;

import com.smi.mstr.transfer.dto.payment.FundsBlockingItemDto;

import java.math.BigDecimal;

public interface FundsBlockingClient {

    FundsBlockingItemDto blockFunds(
            Long modalityId,
            String debitAccountRef,
            BigDecimal amount,
            String currency
    );
}