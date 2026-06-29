package com.smi.mstr.transfer.application.blocking.mock;

import com.smi.mstr.transfer.application.blocking.PaymentBlockingRequest;
import com.smi.mstr.transfer.application.blocking.PaymentBlockingResponse;
import com.smi.mstr.transfer.application.blocking.PaymentBlockingResultStatus;
import com.smi.mstr.transfer.application.blocking.PaymentBlockingService;
import com.smi.mstr.transfer.config.MockPaymentBlockingProperties;
import com.smi.mstr.transfer.domain.enums.PaymentModalityType;
import com.smi.mstr.transfer.domain.enums.PaymentResourceType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "downstream.payment-blocking.mode",
        havingValue = "MOCK",
        matchIfMissing = true
)
public class MockPaymentBlockingService implements PaymentBlockingService {

    private final MockPaymentBlockingProperties properties;

    @Override
    public PaymentBlockingResponse block(PaymentBlockingRequest request) {
        validateRequest(request);
        simulateDelay();

        if (isNotRequired(request)) {
            return notRequired(request);
        }

        String functionalReference = resolveFunctionalReference(request);

        if (contains(functionalReference, properties.getFailOnReferenceContaining())) {
            return failed(request, "Mock blocking forced failure for reference: " + functionalReference);
        }

        if (contains(functionalReference, properties.getInsufficientOnReferenceContaining())) {
            return insufficient(request, "Mock insufficient funds / unavailable resource for reference: " + functionalReference);
        }

        if (contains(functionalReference, properties.getPartialOnReferenceContaining())) {
            return partial(request);
        }

        return blocked(request);
    }

    private PaymentBlockingResponse blocked(PaymentBlockingRequest request) {
        String blockingReference = generateBlockingReference(request);

        return new PaymentBlockingResponse(
                PaymentBlockingResultStatus.BLOCKED,
                blockingReference,
                request.amountToBlock(),
                request.currencyToBlock(),
                "Mock blocking successful.",
                buildPayload(request, PaymentBlockingResultStatus.BLOCKED, blockingReference),
                LocalDateTime.now()
        );
    }

    private PaymentBlockingResponse partial(PaymentBlockingRequest request) {
        String blockingReference = generateBlockingReference(request);

        BigDecimal partialAmount = request.amountToBlock()
                .multiply(new BigDecimal("0.50"))
                .setScale(3, RoundingMode.HALF_UP);

        return new PaymentBlockingResponse(
                PaymentBlockingResultStatus.PARTIALLY_BLOCKED,
                blockingReference,
                partialAmount,
                request.currencyToBlock(),
                "Mock partial blocking: only 50% of the amount was blocked.",
                buildPayload(request, PaymentBlockingResultStatus.PARTIALLY_BLOCKED, blockingReference),
                LocalDateTime.now()
        );
    }

    private PaymentBlockingResponse insufficient(
            PaymentBlockingRequest request,
            String message
    ) {
        return new PaymentBlockingResponse(
                PaymentBlockingResultStatus.INSUFFICIENT_FUNDS,
                null,
                BigDecimal.ZERO,
                request.currencyToBlock(),
                message,
                buildPayload(request, PaymentBlockingResultStatus.INSUFFICIENT_FUNDS, null),
                LocalDateTime.now()
        );
    }

    private PaymentBlockingResponse failed(
            PaymentBlockingRequest request,
            String message
    ) {
        return new PaymentBlockingResponse(
                PaymentBlockingResultStatus.FAILED,
                null,
                BigDecimal.ZERO,
                request.currencyToBlock(),
                message,
                buildPayload(request, PaymentBlockingResultStatus.FAILED, null),
                LocalDateTime.now()
        );
    }

    private PaymentBlockingResponse notRequired(PaymentBlockingRequest request) {
        return new PaymentBlockingResponse(
                PaymentBlockingResultStatus.NOT_REQUIRED,
                null,
                BigDecimal.ZERO,
                request.currencyToBlock(),
                "Blocking is not required for this payment modality.",
                buildPayload(request, PaymentBlockingResultStatus.NOT_REQUIRED, null),
                LocalDateTime.now()
        );
    }

    private boolean isNotRequired(PaymentBlockingRequest request) {
        return request.modalityType() == PaymentModalityType.MANUAL_EXCEPTION;
    }

    private String generateBlockingReference(PaymentBlockingRequest request) {
        String prefix = switch (request.resourceType()) {
            case ACCOUNT -> "BLK-ACC";
            case FX_DEAL -> "BLK-FX";
            case FX_CONTRACT -> "BLK-FWD";
            case FINANCING_FILE -> "BLK-FIN";
            case RECEIVED_FUNDS -> "BLK-RCV";
            case INTERBANK_DEAL -> "BLK-IBD";
            case INTERBANK_COVER -> "BLK-COV";
            case OTHER -> "BLK-OTH";
        };

        return prefix + "-"
                + request.refOperation()
                + "-"
                + request.sequenceNo()
                + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String resolveFunctionalReference(PaymentBlockingRequest request) {
        if (notBlank(request.resourceReference())) {
            return request.resourceReference();
        }

        if (notBlank(request.debitAccountNumber())) {
            return request.debitAccountNumber();
        }

        if (notBlank(request.fxReference())) {
            return request.fxReference();
        }

        return request.operationRef() + "-" + request.sequenceNo();
    }

    private void validateRequest(PaymentBlockingRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Payment blocking request is required.");
        }

        if (request.refOperation() == null) {
            throw new IllegalArgumentException("refOperation is required for payment blocking.");
        }

        if (request.modalityType() == null) {
            throw new IllegalArgumentException("modalityType is required for payment blocking.");
        }

        if (request.resourceType() == null) {
            throw new IllegalArgumentException("resourceType is required for payment blocking.");
        }

        if (request.amountToBlock() == null) {
            throw new IllegalArgumentException("amountToBlock is required for payment blocking.");
        }

        if (request.currencyToBlock() == null || request.currencyToBlock().isBlank()) {
            throw new IllegalArgumentException("currencyToBlock is required for payment blocking.");
        }
    }

    private void simulateDelay() {
        if (properties.getDefaultProcessingDelayMs() <= 0) {
            return;
        }

        try {
            Thread.sleep(properties.getDefaultProcessingDelayMs());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean contains(String value, String token) {
        if (value == null || token == null || token.isBlank()) {
            return false;
        }

        return value.toUpperCase().contains(token.trim().toUpperCase());
    }

    private String buildPayload(
            PaymentBlockingRequest request,
            PaymentBlockingResultStatus status,
            String blockingReference
    ) {
        return """
                {
                  "mock": true,
                  "status": "%s",
                  "blockingReference": "%s",
                  "refOperation": %s,
                  "operationRef": "%s",
                  "sequenceNo": %s,
                  "modalityType": "%s",
                  "resourceType": "%s",
                  "resourceReference": "%s",
                  "amountToBlock": %s,
                  "currencyToBlock": "%s",
                  "generatedAt": "%s"
                }
                """.formatted(
                status,
                nullToEmpty(blockingReference),
                request.refOperation(),
                nullToEmpty(request.operationRef()),
                request.sequenceNo(),
                request.modalityType(),
                request.resourceType(),
                nullToEmpty(request.resourceReference()),
                request.amountToBlock(),
                nullToEmpty(request.currencyToBlock()),
                LocalDateTime.now()
        );
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private String nullToEmpty(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}