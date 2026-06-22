package com.smi.mstr.transfer.dto;

import com.smi.mstr.transfer.dto.normalized.AccountDto;
import com.smi.mstr.transfer.dto.normalized.PartyDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record UpdateDebtorRequest(
        String updatedBy,
        String comment,

        @Valid PartyDto ultimateDebtor,

        @NotNull
        @Valid PartyDto debtor,

        @Valid AccountDto chargesAccount
) {}