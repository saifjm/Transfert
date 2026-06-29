package com.smi.mstr.transfer.application.blocking;

import com.smi.mstr.transfer.domain.enums.PaymentModalityType;
import com.smi.mstr.transfer.domain.enums.PaymentResourceType;

import java.math.BigDecimal;

public record PaymentBlockingRequest(

        Long refOperation,
        String operationRef,

        Long idPaymentModality,
        Integer sequenceNo,

        PaymentModalityType modalityType,
        PaymentResourceType resourceType,

        String resourceReference,

        String debitAccountNumber,
        String debitAccountCurrency,
        BigDecimal debitAmount,

        BigDecimal coveredTransferAmount,
        String coveredTransferCurrency,

        String fxReference,

        BigDecimal amountToBlock,
        String currencyToBlock,

        String branchCode,
        String correlationId
) {
}
