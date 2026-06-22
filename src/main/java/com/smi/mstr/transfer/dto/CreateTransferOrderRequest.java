package com.smi.mstr.transfer.dto;

import com.smi.mstr.transfer.domain.enums.TransferType;
import com.smi.mstr.transfer.dto.normalized.AccountDto;
import com.smi.mstr.transfer.dto.normalized.FinancialAgentDto;
import com.smi.mstr.transfer.dto.normalized.PartyDto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTransferOrderRequest(

        TransferType transferType,

        PartyDto ultimateDebtor,
        PartyDto debtor,
        PartyDto creditor,
        PartyDto ultimateCreditor,

        AccountDto creditorAccount,
        AccountDto chargesAccount,

        FinancialAgentDto creditorAgent,

        String endToEndId,

        BigDecimal orderAmount,
        String orderCurrency,

        BigDecimal transferAmount,
        String transferCurrency,

        LocalDate valueDate,

        BigDecimal fxRate,
        BigDecimal counterValueTnd,

        String swiftPriority,
        String serviceLevelCode,
        String categoryPurposeCode,

        String purposeCode,
        String purposeProprietary,
        String remittanceUnstructured,

        String chargeBearer
) {
}