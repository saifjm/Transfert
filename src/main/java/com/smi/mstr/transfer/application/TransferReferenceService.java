package com.smi.mstr.transfer.application;

import com.smi.mstr.transfer.domain.repository.MvtTrOperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferReferenceService {

    private final MvtTrOperationRepository operationRepository;

    public String generateReference() {
        String year = String.valueOf(Year.now().getValue());
        String uniquePart = UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();

        return "TR-" + year + "-" + uniquePart;
    }

    public void ensureUnique(String operationRef) {
        if (operationRepository.existsByOperationRef(operationRef)) {
            throw new IllegalStateException(
                    "Duplicate transfer operation reference: " + operationRef
            );
        }
    }
}