package com.smi.mstr.transfer.dto.payment;

import com.smi.mstr.transfer.domain.enums.PaymentModalityType;
import com.smi.mstr.transfer.domain.enums.PaymentResourceAvailabilityStatus;
import com.smi.mstr.transfer.domain.enums.PaymentSecurityStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PaymentModalityDto(
        Long modalityId,

        @NotNull PaymentModalityType modalityType,
        Integer sequenceNo,

        BigDecimal sourceAmount,
        String sourceCurrency,

        BigDecimal targetAmount,
        String targetCurrency,

        String debitAccountRef,
        String debitAccountCurrency,

        String fxMode,
        BigDecimal fxRate,
        String fxDealRef,
        String forwardContractRef,

        String financingRef,
        String receivedFundsRef,
        String interbankCoverRef,
        String counterpartyBankBic,
        LocalDate valueDate,

        PaymentResourceAvailabilityStatus availabilityStatus,
        BigDecimal availableAmount,
        String availableCurrency,
        LocalDateTime availabilityCheckedAt,
        String availabilityMessage,

        PaymentSecurityStatus securityStatus
) {}