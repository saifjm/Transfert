package com.smi.mstr.transfer.dto;

import com.smi.mstr.transfer.dto.normalized.AccountDto;
import com.smi.mstr.transfer.dto.normalized.FinancialAgentDto;
import com.smi.mstr.transfer.dto.normalized.PartyDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateCreditorRequest(
        @NotBlank String updatedBy,
        String comment,

        @NotNull @Valid PartyDto creditor,
        @NotNull @Valid AccountDto creditorAccount,
        @NotNull @Valid FinancialAgentDto creditorAgent
) {}