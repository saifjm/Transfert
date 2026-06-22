package com.smi.mstr.transfer.dto.interbank;

import com.smi.mstr.transfer.domain.enums.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record InterbankEnrichmentResponse(
        String operationRef,
        Long refOperation,

        String transferCurrency,
        BigDecimal transferAmount,
        LocalDate valueDate,
        String chargeBearer,

        InterbankEnrichmentStatus enrichmentStatus,
        PaymentPathStatus paymentPathStatus,
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

        String pacs008MessageId,
        String pacs009CovMessageId,
        String uetr,

        List<InterbankAgentDto> paymentPath,
        List<InterbankInstructionDto> instructions,
        List<InterbankValidationErrorDto> warnings
) {
}