package com.smi.mstr.transfer.dto.normalized;

import jakarta.validation.constraints.NotBlank;

public record PartyIdentificationDto(
        @NotBlank String identificationScope,
        @NotBlank String identificationType,
        @NotBlank String identificationValue,
        String issuer,
        String schemeNameCode,
        String schemeNameProprietary
) {}
