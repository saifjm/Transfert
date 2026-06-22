package com.smi.mstr.transfer.application;

import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.domain.repository.MvtTrOperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransferOperationLookupService {

    private final MvtTrOperationRepository operationRepository;

    @Transactional(readOnly = true)
    public MvtTrOperation findByReference(String operationRef) {
        if (operationRef == null || operationRef.isBlank()) {
            throw new IllegalArgumentException("Transfer operation reference is required.");
        }

        String value = operationRef.trim();

        return operationRepository.findByRefOrdre(value)
                .or(() -> findByNumericRefOperation(value))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transfer operation not found: " + operationRef
                ));
    }

    private Optional<MvtTrOperation> findByNumericRefOperation(String value) {
        try {
            return operationRepository.findByRefOperation(Long.valueOf(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}