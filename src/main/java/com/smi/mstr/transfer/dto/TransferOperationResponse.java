package com.smi.mstr.transfer.dto;

import com.smi.mstr.transfer.domain.enums.OriginChannel;
import com.smi.mstr.transfer.domain.enums.SwiftPriority;
import com.smi.mstr.transfer.domain.enums.TransferOperationStatus;
import com.smi.mstr.transfer.domain.enums.TransferType;
import com.smi.mstr.transfer.dto.normalized.AccountDto;
import com.smi.mstr.transfer.dto.normalized.FinancialAgentDto;
import com.smi.mstr.transfer.dto.normalized.PartyDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransferOperationResponse(
        Long refOperation,

        String operationRef,
        String refOrdre,
        String numDossier,

        LocalDate dateOperation,
        LocalDate dateDossier,

        Long codeOperation,
        String branchCode,

        TransferOperationStatus status,
        TransferType transferType,
        SwiftPriority swiftPriority,

        PartyDto ultimateDebtor,
        PartyDto debtor,
        PartyDto creditor,
        PartyDto ultimateCreditor,

        AccountDto creditorAccount,
        AccountDto chargesAccount,

        FinancialAgentDto creditorAgent,

        Long ultimateDebtorId,
        Long debtorId,
        Long creditorId,
        Long ultimateCreditorId,

        String noCompteCommission,
        String noCompteCreditor,

        String endToEndId,
        String transactionId,
        String uetr,

        BigDecimal orderAmount,
        String orderCurrency,

        BigDecimal transferAmount,
        String transferCurrency,

        LocalDate valueDate,

        BigDecimal fxRate,
        BigDecimal counterValueTnd,

        String serviceLevelCode,
        String localInstrumentCode,
        String categoryPurposeCode,

        String purposeCode,
        String purposeProprietary,
        String remittanceUnstructured,

        String chargeBearer,

        OriginChannel sourceChannel,
        String sourceModule,
        String sourceReference,

        String workflowInstanceId,
        String workflowTaskId,
        String workflowContextJson,

        Long version,
        LocalDateTime createdAt,
        LocalDate dateValidation
) {}