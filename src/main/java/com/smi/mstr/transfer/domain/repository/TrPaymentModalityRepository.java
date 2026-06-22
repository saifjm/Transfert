package com.smi.mstr.transfer.domain.repository;

import com.smi.mstr.transfer.domain.entity.TrPaymentModality;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrPaymentModalityRepository
        extends JpaRepository<TrPaymentModality, Long> {

    List<TrPaymentModality> findByOperation_RefOrdreOrderBySequenceNoAsc(
            String refOrdre
    );

    List<TrPaymentModality> findByOperation_RefOperationOrderBySequenceNoAsc(
            Long refOperation
    );

    void deleteByOperation_RefOperation(Long refOperation);

    @Deprecated
    default List<TrPaymentModality> findByOperation_OperationRefOrderBySequenceNoAsc(
            String operationRef
    ) {
        return findByOperation_RefOrdreOrderBySequenceNoAsc(operationRef);
    }
}