package com.smi.mstr.transfer.dto.payment;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UpdatePaymentModalitiesRequest(
        @NotBlank String updatedBy,
        String comment,

        @NotEmpty @Valid List<PaymentModalityDto> modalities
) {}