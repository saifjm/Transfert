package com.smi.mstr.transfer.dto;

import com.smi.mstr.transfer.domain.enums.*;
import com.smi.mstr.transfer.dto.normalized.AccountDto;
import com.smi.mstr.transfer.dto.normalized.FinancialAgentDto;
import com.smi.mstr.transfer.dto.normalized.PartyDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransferOperationResponse(
        Long refOperation,
        String operationRef,
        TransferOperationStatus status,
        CompletionStatus completionStatus,
        TransferType transferType,
        SwiftPriority swiftPriority,

        String branchCode,
        String numDossier,
        LocalDate dateOperation,
        LocalDate dateDossier,

        BigDecimal orderAmount,
        String orderCurrency,
        BigDecimal transferAmount,
        String transferCurrency,

        String purposeCode,
        String purposeProprietary,
        String remittanceUnstructured,
        String chargeBearer,

        PartyDto debtor,
        AccountDto debtorAccount,
        PartyDto creditor,
        AccountDto creditorAccount,
        FinancialAgentDto creditorAgent,

        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long version
) {}