package com.smi.mstr.transfer.dto;

import com.smi.mstr.transfer.dto.normalized.AccountDto;
import com.smi.mstr.transfer.dto.normalized.FinancialAgentDto;
import com.smi.mstr.transfer.dto.normalized.PartyDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record UpdateCreditorRequest(
        String updatedBy,
        String comment,

        @NotNull
        @Valid PartyDto creditor,

        @Valid PartyDto ultimateCreditor,

        @Valid AccountDto creditorAccount,

        @Valid FinancialAgentDto creditorAgent
) {}