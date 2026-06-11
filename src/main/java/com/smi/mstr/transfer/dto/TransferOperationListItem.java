package com.smi.mstr.transfer.dto;

import com.smi.mstr.transfer.domain.enums.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransferOperationListItem(
        Long refOperation,
        String operationRef,
        TransferOperationStatus status,
        CompletionStatus completionStatus,
        TransferType transferType,
        SwiftPriority swiftPriority,
        String branchCode,
        String numDossier,
        LocalDate dateOperation,
        String createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}