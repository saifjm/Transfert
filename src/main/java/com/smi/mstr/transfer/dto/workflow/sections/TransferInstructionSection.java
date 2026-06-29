package com.smi.mstr.transfer.dto.workflow.sections;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransferInstructionSection(
        BigDecimal orderAmount,
        String orderCurrency,

        BigDecimal transferAmount,
        String transferCurrency,

        LocalDate valueDate,

        BigDecimal fxRate,
        BigDecimal counterValueTnd,

        String endToEndId,

        String swiftPriority,
        String serviceLevelCode,
        String categoryPurposeCode,

        String purposeCode,
        String purposeProprietary,
        String remittanceUnstructured,

        String chargeBearer
) {
}