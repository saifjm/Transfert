package com.smi.mstr.transfer.application;

import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.domain.enums.TransferType;
import com.smi.mstr.transfer.domain.repository.MvtTrOperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferReferenceService {

    private static final int REF_ORDRE_MAX_LENGTH = 35;

    private final MvtTrOperationRepository operationRepository;

    public String generateNumDossier(MvtTrOperation operation) {
        requirePersistedOperation(operation);
        return String.format("%010d", operation.getRefOperation());
    }

    public String generateRefOrdre(MvtTrOperation operation) {
        requirePersistedOperation(operation);

        String prefix = resolvePrefix(operation);
        String year = String.valueOf(Year.now().getValue());

        String numDossier = operation.getNumDossier();

        if (isBlank(numDossier)) {
            numDossier = generateNumDossier(operation);
        }

        numDossier = normalizeNumDossier(numDossier);

        String sequencePart = String.format("%012d", operation.getRefOperation());

        String refOrdre = prefix + "-" + year + "-" + numDossier + "-" + sequencePart;

        if (refOrdre.length() > REF_ORDRE_MAX_LENGTH) {
            throw new IllegalStateException(
                    "Generated REF_ORDRE exceeds "
                            + REF_ORDRE_MAX_LENGTH
                            + " characters: "
                            + refOrdre
                            + " ("
                            + refOrdre.length()
                            + ")"
            );
        }

        return refOrdre;
    }

    public void ensureUnique(MvtTrOperation operation) {
        requirePersistedOperation(operation);

        String refOrdre = operation.getRefOrdre();

        if (isBlank(refOrdre)) {
            throw new IllegalArgumentException("REF_ORDRE is required.");
        }

        boolean existsOnAnotherOperation =
                operationRepository.existsByRefOrdreAndRefOperationNot(
                        refOrdre,
                        operation.getRefOperation()
                );

        if (existsOnAnotherOperation) {
            throw new IllegalStateException(
                    "Transfer order reference already exists on another operation: "
                            + refOrdre
            );
        }
    }

    public String generateEndToEndId(MvtTrOperation operation) {
        requirePersistedOperation(operation);

        String prefix = resolvePrefix(operation);
        String sequencePart = String.format("%012d", operation.getRefOperation());

        return "E2E-" + prefix + "-" + sequencePart;
    }

    public String generateTransactionId(MvtTrOperation operation) {
        requirePersistedOperation(operation);

        String prefix = resolvePrefix(operation);
        String sequencePart = String.format("%012d", operation.getRefOperation());

        return "TX-" + prefix + "-" + sequencePart;
    }

    public String generateUetr() {
        return UUID.randomUUID().toString();
    }

    public String generateCorrelationId() {
        return "CORR-MS-TR-" + UUID.randomUUID();
    }

    private String resolvePrefix(MvtTrOperation operation) {
        if (operation.getTypeTransfert() == TransferType.F) {
            return "OPF";
        }

        return "OPC";
    }

    private String normalizeNumDossier(String numDossier) {
        String cleaned = numDossier.trim();

        if (cleaned.length() > 10) {
            return cleaned.substring(cleaned.length() - 10);
        }

        return String.format("%10s", cleaned).replace(' ', '0');
    }

    private void requirePersistedOperation(MvtTrOperation operation) {
        if (operation == null) {
            throw new IllegalArgumentException("Operation is required.");
        }

        if (operation.getRefOperation() == null) {
            throw new IllegalStateException(
                    "Operation must be saved before generating references."
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}