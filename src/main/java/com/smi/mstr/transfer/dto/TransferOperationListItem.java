package com.smi.mstr.transfer.dto;

import com.smi.mstr.transfer.domain.enums.OriginChannel;
import com.smi.mstr.transfer.domain.enums.SwiftPriority;
import com.smi.mstr.transfer.domain.enums.TransferOperationStatus;
import com.smi.mstr.transfer.domain.enums.TransferType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransferOperationListItem(
        Long refOperation,

        /**
         * Correspond au nouveau champ REF_ORDRE.
         * On garde le nom operationRef côté API pour éviter de casser le frontend.
         */
        String operationRef,

        TransferOperationStatus status,

        TransferType transferType,

        SwiftPriority swiftPriority,

        /**
         * Correspond au nouveau champ CODE_AGENCE.
         */
        String branchCode,

        String numDossier,
        LocalDate dateOperation,
        LocalDate dateDossier,

        BigDecimal orderAmount,
        String orderCurrency,

        BigDecimal transferAmount,
        String transferCurrency,

        LocalDate valueDate,

        OriginChannel sourceChannel,
        String sourceModule,
        String sourceReference,

        LocalDateTime createdAt,
        LocalDate dateValidation
) {}