package com.smi.mstr.transfer.dto.payment;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record SecurePaymentRequest(
        @NotBlank String securedBy,

        BigDecimal estimatedFeesAmount,
        String estimatedFeesCurrency
) {}