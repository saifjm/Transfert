package com.smi.mstr.transfer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateTransferQualificationRequest(
        @NotBlank String updatedBy,
        String comment,

        @NotNull BigDecimal orderAmount,
        @NotBlank String orderCurrency,

        @NotNull BigDecimal transferAmount,
        @NotBlank String transferCurrency,

        String purposeCode,
        String purposeProprietary,
        String remittanceUnstructured,
        String regulatoryNatureCode,
        String chargeBearer
) {}