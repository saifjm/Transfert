package com.smi.mstr.transfer.dto;

import com.smi.mstr.transfer.domain.enums.SwiftPriority;
import com.smi.mstr.transfer.dto.normalized.AccountDto;
import com.smi.mstr.transfer.dto.normalized.FinancialAgentDto;
import com.smi.mstr.transfer.dto.normalized.PartyDto;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SaveTransferDraftRequest(
        String updatedBy,
        String comment,

        @Valid PartyDto ultimateDebtor,
        @Valid PartyDto debtor,
        @Valid PartyDto creditor,
        @Valid PartyDto ultimateCreditor,

        @Valid AccountDto creditorAccount,
        @Valid AccountDto chargesAccount,
        @Valid FinancialAgentDto creditorAgent,

        BigDecimal orderAmount,
        String orderCurrency,

        BigDecimal transferAmount,
        String transferCurrency,

        LocalDate valueDate,

        BigDecimal fxRate,
        BigDecimal counterValueTnd,

        SwiftPriority swiftPriority,
        String serviceLevelCode,
        String localInstrumentCode,
        String categoryPurposeCode,

        String purposeCode,
        String purposeProprietary,
        String remittanceUnstructured,

        String chargeBearer
) {}