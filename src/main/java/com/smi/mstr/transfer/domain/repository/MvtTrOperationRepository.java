package com.smi.mstr.transfer.domain.repository;

import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.domain.enums.TransferOperationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MvtTrOperationRepository extends JpaRepository<MvtTrOperation, Long> {

    Optional<MvtTrOperation> findByOperationRef(String operationRef);

    boolean existsByOperationRef(String operationRef);

    List<MvtTrOperation> findByStatusOrderByCreatedAtDesc(TransferOperationStatus status);

    List<MvtTrOperation> findByBranchCodeAndStatusOrderByCreatedAtDesc(
            String branchCode,
            TransferOperationStatus status
    );

    @EntityGraph(attributePaths = {
            "parties",
            "parties.postalAddresses",
            "parties.identifications",
            "accounts",
            "financialAgents"
    })
    Optional<MvtTrOperation> findDetailedByOperationRef(String operationRef);
}