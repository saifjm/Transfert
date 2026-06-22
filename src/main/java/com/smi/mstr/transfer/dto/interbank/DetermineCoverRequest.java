package com.smi.mstr.transfer.dto.interbank;

import com.smi.mstr.transfer.domain.enums.SettlementMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DetermineCoverRequest(
        String requestedBy,
        String targetFormat,
        Boolean forceCover,

        SettlementMethod settlementMethod,
        String settlementAccountRef,
        String settlementCurrency,
        BigDecimal settlementAmount,
        LocalDate settlementDate,

        String nostroAccountRef,
        String nostroCurrency,
        String nostroAgentBic
) {
}