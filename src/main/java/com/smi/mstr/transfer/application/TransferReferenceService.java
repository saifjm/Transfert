package com.smi.mstr.transfer.application;

import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.domain.repository.MvtTrOperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Year;

@Service
@RequiredArgsConstructor
public class TransferReferenceService {

    private final MvtTrOperationRepository operationRepository;

    public String generateNumDossier(MvtTrOperation operation) {
        return String.format(
                "%010d",
                operation.getRefOperation()
        );
    }

    public String generateRefOrdre(MvtTrOperation operation) {
        String prefix = resolvePrefix(operation);
        String year = String.valueOf(Year.now().getValue());

        String numDossier = operation.getNumDossier();
        if (numDossier == null || numDossier.isBlank()) {
            numDossier = generateNumDossier(operation);
        }

        String refOperationPart = String.format("%015d", operation.getRefOperation());

        return prefix + "/" + year + "/" + numDossier + "/" + refOperationPart;
    }

    public void ensureUnique(String refOrdre) {
        if (operationRepository.existsByRefOrdre(refOrdre)) {
            throw new IllegalStateException(
                    "Transfer order reference already exists: " + refOrdre
            );
        }
    }

    public String generateEndToEndId(MvtTrOperation operation) {
        return "E2E-" + operation.getRefOperation();
    }

    public String generateUetr() {
        return java.util.UUID.randomUUID().toString();
    }

    private String resolvePrefix(MvtTrOperation operation) {
        if (operation.getTypeTransfert() == null) {
            return "OP";
        }

        return switch (operation.getTypeTransfert()) {
            case C -> "OPC";
            case F -> "OPF";
        };
    }
}