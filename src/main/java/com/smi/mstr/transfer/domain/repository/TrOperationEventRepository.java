package com.smi.mstr.transfer.domain.repository;

import com.smi.mstr.transfer.domain.entity.TrOperationEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrOperationEventRepository
        extends JpaRepository<TrOperationEvent, Long> {

    List<TrOperationEvent> findByOperation_RefOperationOrderByActionAtAsc(
            Long refOperation
    );
}