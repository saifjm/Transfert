package com.smi.mstr.transfer.dto.payment;

import com.smi.mstr.transfer.domain.enums.PaymentResourceAvailabilityStatus;
import com.smi.mstr.transfer.domain.enums.PaymentSecurityStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PaymentValidatorReviewReport(
        String operationRef,

        BigDecimal transferAmount,
        String transferCurrency,

        PaymentResourceAvailabilityStatus overallAvailabilityStatus,
        PaymentSecurityStatus overallSecurityStatus,

        boolean canProceedToValidation,

        LocalDateTime viewedAt,

        List<PaymentModalityValidatorViewDto> modalities,
        List<PaymentValidationAnomalyDto> anomalies
) {}