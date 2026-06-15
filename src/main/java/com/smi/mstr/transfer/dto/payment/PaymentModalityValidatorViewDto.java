package com.smi.mstr.transfer.dto.payment;

import com.smi.mstr.transfer.domain.enums.PaymentImpactAction;
import com.smi.mstr.transfer.domain.enums.PaymentImpactTarget;
import com.smi.mstr.transfer.domain.enums.PaymentModalityType;
import com.smi.mstr.transfer.domain.enums.PaymentResourceAvailabilityStatus;
import com.smi.mstr.transfer.domain.enums.PaymentResourceType;
import com.smi.mstr.transfer.domain.enums.PaymentSecurityStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PaymentModalityValidatorViewDto(
        Long modalityId,
        Integer sequenceNo,
        PaymentModalityType modalityType,

        BigDecimal sharePercent,

        BigDecimal targetAmount,
        String targetCurrency,

        PaymentResourceType resourceType,
        PaymentImpactTarget impactTarget,
        PaymentImpactAction impactAction,
        String resourceRef,

        PaymentResourceAvailabilityStatus availabilityStatus,
        BigDecimal availableAmount,
        String availableCurrency,
        LocalDateTime availabilityCheckedAt,
        String availabilityMessage,

        PaymentSecurityStatus securityStatus,
        List<PaymentSecurityItemDto> securityItems
) {}