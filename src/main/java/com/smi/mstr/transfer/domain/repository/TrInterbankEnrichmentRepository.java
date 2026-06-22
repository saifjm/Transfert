package com.smi.mstr.transfer.domain.repository;

import com.smi.mstr.transfer.domain.entity.TrInterbankEnrichment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrInterbankEnrichmentRepository
        extends JpaRepository<TrInterbankEnrichment, Long> {

    Optional<TrInterbankEnrichment> findByOperation_RefOperation(Long refOperation);
}