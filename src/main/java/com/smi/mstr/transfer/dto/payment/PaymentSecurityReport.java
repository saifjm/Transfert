package com.smi.mstr.transfer.dto.payment;

import com.smi.mstr.transfer.domain.enums.PaymentSecurityStatus;

import java.time.LocalDateTime;
import java.util.List;

public record PaymentSecurityReport(
        String operationRef,
        PaymentSecurityStatus overallStatus,
        LocalDateTime securedAt,
        List<PaymentSecurityItemDto> results
) {}