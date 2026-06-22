package com.smi.mstr.transfer.domain.repository;

import com.smi.mstr.transfer.domain.entity.TrOperationValidationError;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrOperationValidationErrorRepository
        extends JpaRepository<TrOperationValidationError, Long> {

    void deleteByOperation_RefOperation(Long refOperation);

    List<TrOperationValidationError> findByOperation_RefOperationOrderBySectionAscFieldPathAsc(
            Long refOperation
    );
}