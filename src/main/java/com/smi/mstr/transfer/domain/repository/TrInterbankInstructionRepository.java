package com.smi.mstr.transfer.domain.repository;

import com.smi.mstr.transfer.domain.entity.TrInterbankInstruction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrInterbankInstructionRepository
        extends JpaRepository<TrInterbankInstruction, Long> {

    List<TrInterbankInstruction> findByOperation_RefOperationOrderByInstructionIdAsc(
            Long refOperation
    );

    void deleteByOperation_RefOperation(Long refOperation);
}