package com.smi.mstr.transfer.application;

import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.domain.enums.TransferOperationStatus;
import com.smi.mstr.transfer.domain.repository.MvtTrOperationRepository;
import com.smi.mstr.transfer.dto.TransferOperationListItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferOperationQueryService {

    private final MvtTrOperationRepository operationRepository;

    public List<TransferOperationListItem> findInProgressOrders(String branchCode) {
        List<MvtTrOperation> operations;

        if (branchCode == null || branchCode.isBlank()) {
            operations = operationRepository.findByStatusOrderByCreatedAtDesc(TransferOperationStatus.X);
        } else {
            operations = operationRepository.findByBranchCodeAndStatusOrderByCreatedAtDesc(
                    branchCode,
                    TransferOperationStatus.X
            );
        }

        return operations.stream()
                .map(op -> new TransferOperationListItem(
                        op.getRefOperation(),
                        op.getOperationRef(),
                        op.getStatus(),
                        op.getCompletionStatus(),
                        op.getTransferType(),
                        op.getSwiftPriority(),
                        op.getBranchCode(),
                        op.getNumDossier(),
                        op.getDateOperation(),
                        op.getCreatedBy(),
                        op.getCreatedAt(),
                        op.getUpdatedAt()
                ))
                .toList();
    }
}