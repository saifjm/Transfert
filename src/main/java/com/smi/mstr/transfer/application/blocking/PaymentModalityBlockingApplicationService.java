package com.smi.mstr.transfer.application.blocking;

import com.smi.mstr.transfer.application.context.WorkflowCommandContext;
import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.domain.entity.TrPaymentModality;
import com.smi.mstr.transfer.domain.enums.BlockingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentModalityBlockingApplicationService {

    private final PaymentBlockingService paymentBlockingService;

    public PaymentBlockingReport blockAll(
            MvtTrOperation operation,
            WorkflowCommandContext context
    ) {
        validateOperation(operation);

        List<PaymentBlockingIssue> issues = new ArrayList<>();

        if (operation.getPaymentModalities() == null
                || operation.getPaymentModalities().isEmpty()) {
            issues.add(new PaymentBlockingIssue(
                    "PAYMOD_EMPTY",
                    "paymentModalities",
                    "No payment modalities found to block.",
                    null,
                    null
            ));

            return new PaymentBlockingReport(issues);
        }

        for (TrPaymentModality modality : operation.getPaymentModalities()) {
            PaymentBlockingResponse response = blockOne(operation, modality, context);

            applyResponse(modality, response);

            if (response.failed()) {
                issues.add(new PaymentBlockingIssue(
                        response.status().name(),
                        "paymentModalities[" + modality.getSequenceNo() + "]",
                        response.message(),
                        modality.getSequenceNo(),
                        modality.getModalityType()
                ));
            }

            if (response.status() == PaymentBlockingResultStatus.PARTIALLY_BLOCKED) {
                issues.add(new PaymentBlockingIssue(
                        "PARTIALLY_BLOCKED",
                        "paymentModalities[" + modality.getSequenceNo() + "]",
                        response.message(),
                        modality.getSequenceNo(),
                        modality.getModalityType()
                ));
            }
        }

        return new PaymentBlockingReport(issues);
    }

    public void blockAllOrThrow(
            MvtTrOperation operation,
            WorkflowCommandContext context
    ) {
        PaymentBlockingReport report = blockAll(operation, context);

        if (report.hasErrors()) {
            throw new PaymentBlockingException(report);
        }
    }

    private PaymentBlockingResponse blockOne(
            MvtTrOperation operation,
            TrPaymentModality modality,
            WorkflowCommandContext context
    ) {
        if (!modality.requiresBlocking()) {
            return new PaymentBlockingResponse(
                    PaymentBlockingResultStatus.NOT_REQUIRED,
                    null,
                    BigDecimal.ZERO,
                    resolveCurrencyToBlock(modality),
                    "Blocking not required.",
                    null,
                    null
            );
        }

        PaymentBlockingRequest request = new PaymentBlockingRequest(
                operation.getRefOperation(),
                operation.getRefOrdre(),

                modality.getIdPaymentModality(),
                modality.getSequenceNo(),

                modality.getModalityType(),
                modality.getResourceType(),

                modality.getResourceReference(),

                modality.getDebitAccountNumber(),
                modality.getDebitAccountCurrency(),
                modality.getDebitAmount(),

                modality.getCoveredTransferAmount(),
                modality.getCoveredTransferCurrency(),

                modality.getFxReference(),

                resolveAmountToBlock(modality),
                resolveCurrencyToBlock(modality),

                context == null ? operation.getCodeAgence() : context.branchCode(),
                context == null ? operation.getCorrelationId() : context.correlationId()
        );

        return paymentBlockingService.block(request);
    }

    private void applyResponse(
            TrPaymentModality modality,
            PaymentBlockingResponse response
    ) {
        if (response == null) {
            modality.markBlockingFailed("Blocking response is null.");
            return;
        }

        switch (response.status()) {
            case BLOCKED -> modality.markBlocked(
                    response.blockingReference(),
                    response.blockedAmount(),
                    response.blockedCurrency()
            );

            case PARTIALLY_BLOCKED -> {
                modality.setBlockingStatus(BlockingStatus.PARTIALLY_BLOCKED);
                modality.setBlockingReference(response.blockingReference());
                modality.setBlockedAmount(response.blockedAmount());
                modality.setBlockedCurrency(response.blockedCurrency());
                modality.setBlockedAt(response.blockedAt());
                modality.setModalitySnapshotJson(response.rawPayload());
            }

            case NOT_REQUIRED -> {
                modality.setBlockingStatus(BlockingStatus.NOT_REQUIRED);
                modality.setBlockedAmount(BigDecimal.ZERO);
                modality.setBlockedCurrency(response.blockedCurrency());
                modality.setModalitySnapshotJson(response.rawPayload());
            }

            case INSUFFICIENT_FUNDS, FAILED -> modality.markBlockingFailed(response.message());
        }

        if (response.rawPayload() != null) {
            modality.setModalitySnapshotJson(response.rawPayload());
        }
    }

    private BigDecimal resolveAmountToBlock(TrPaymentModality modality) {
        if (modality.getDebitAmount() != null
                && modality.getDebitAmount().compareTo(BigDecimal.ZERO) > 0) {
            return modality.getDebitAmount();
        }

        if (modality.getCoveredTransferAmount() != null) {
            return modality.getCoveredTransferAmount();
        }

        return BigDecimal.ZERO;
    }

    private String resolveCurrencyToBlock(TrPaymentModality modality) {
        if (modality.getDebitAccountCurrency() != null
                && !modality.getDebitAccountCurrency().isBlank()) {
            return modality.getDebitAccountCurrency();
        }

        return modality.getCoveredTransferCurrency();
    }

    private void validateOperation(MvtTrOperation operation) {
        if (operation == null) {
            throw new IllegalArgumentException("Operation is required for payment blocking.");
        }

        if (operation.getRefOperation() == null) {
            throw new IllegalStateException("Operation must be persisted before payment blocking.");
        }
    }
}
