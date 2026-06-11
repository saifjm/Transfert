package com.smi.mstr.transfer.dto;

import jakarta.validation.constraints.NotBlank;

public record RunToiletteControlRequest(
        @NotBlank String controlledBy
) {}
