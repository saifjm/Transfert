package com.smi.mstr.transfer.dto.payment;

import com.smi.mstr.transfer.domain.enums.FxType;
import com.smi.mstr.transfer.domain.enums.PaymentModalityType;

import java.math.BigDecimal;

public record PaymentModalityCommandDto(
        Integer sequenceNo,
        PaymentModalityType modalityType,

        BigDecimal coveragePercentage,
        BigDecimal coveredTransferAmount,
        String coveredTransferCurrency,

        String debitAccountNumber,
        String debitAccountCurrency,
        BigDecimal debitAmount,

        FxType fxType,
        BigDecimal fxRate,
        String fxReference,

        String resourceReference
) {
}