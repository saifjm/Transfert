package com.smi.mstr.transfer.dto;

import com.smi.mstr.transfer.dto.normalized.AccountDto;
import com.smi.mstr.transfer.dto.normalized.FinancialAgentDto;
import com.smi.mstr.transfer.dto.normalized.PartyDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SaveTransferDraftRequest(
        @NotBlank String updatedBy,
        String comment,

        BigDecimal orderAmount,
        String orderCurrency,

        BigDecimal transferAmount,
        String transferCurrency,

        String purposeCode,
        String purposeProprietary,
        String remittanceUnstructured,
        String chargeBearer,

        @NotNull @Valid PartyDto debtor,
        @NotNull @Valid AccountDto debtorAccount,

        @NotNull @Valid PartyDto creditor,
        @NotNull @Valid AccountDto creditorAccount,

        @NotNull @Valid FinancialAgentDto creditorAgent
) {}