package com.smi.mstr.transfer.dto;

import com.smi.mstr.transfer.dto.normalized.AccountDto;
import com.smi.mstr.transfer.dto.normalized.PartyDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateDebtorRequest(
        @NotBlank String updatedBy,
        String comment,

        @NotNull @Valid PartyDto debtor,
        @NotNull @Valid AccountDto debtorAccount
) {}