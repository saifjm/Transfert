package com.smi.mstr.transfer.application.ref;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RefInterbankChainRequest(
        String sourceOperationRef,
        String transferCurrency,
        BigDecimal transferAmount,
        LocalDate settlementDate,
        String debtorAgentBic,
        String creditorAgentBic,
        String creditorCountry,
        String chargeBearer
) {
}