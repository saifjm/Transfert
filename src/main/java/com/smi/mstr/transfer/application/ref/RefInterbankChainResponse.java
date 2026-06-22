package com.smi.mstr.transfer.application.ref;

import com.smi.mstr.transfer.domain.enums.CoverMessageType;
import com.smi.mstr.transfer.domain.enums.PaymentRouteType;
import com.smi.mstr.transfer.domain.enums.SettlementMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RefInterbankChainResponse(
        String proposalId,
        String refVersion,

        PaymentRouteType paymentRouteType,
        SettlementMethod settlementMethod,

        String settlementAccountRef,
        String settlementCurrency,
        BigDecimal settlementAmount,
        LocalDate settlementDate,

        String nostroAccountRef,
        String nostroCurrency,
        String nostroAgentBic,

        boolean coverRequired,
        CoverMessageType coverMessageType,
        String coverReason,

        List<RefInterbankAgentDto> agents,
        List<String> warnings
) {
}