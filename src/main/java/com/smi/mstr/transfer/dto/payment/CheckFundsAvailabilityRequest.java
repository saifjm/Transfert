package com.smi.mstr.transfer.dto.payment;

import jakarta.validation.constraints.NotBlank;

public record CheckFundsAvailabilityRequest(
        @NotBlank String checkedBy
) {}
