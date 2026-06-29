package com.smi.mstr.transfer.application.validation.payment;

import com.smi.mstr.transfer.application.context.WorkflowCommandContext;
import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.domain.entity.TrPaymentModality;
import com.smi.mstr.transfer.domain.enums.PaymentModalityType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PaymentModalityValidationService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal AMOUNT_TOLERANCE = new BigDecimal("0.001");
    private static final BigDecimal PERCENTAGE_TOLERANCE = new BigDecimal("0.000001");

    private final PaymentModalityValidationRegistry registry;

    public PaymentModalityValidationReport validate(
            MvtTrOperation operation,
            WorkflowCommandContext workflowContext
    ) {
        PaymentModalityValidationCollector collector =
                new PaymentModalityValidationCollector();

        validateOperationHeader(operation, collector);

        List<TrPaymentModality> modalities =
                operation == null || operation.getPaymentModalities() == null
                        ? List.of()
                        : operation.getPaymentModalities();

        if (modalities.isEmpty()) {
            collector.globalError(
                    "PAYMOD_EMPTY",
                    "paymentModalities",
                    "At least one payment modality is required for a transfer order."
            );

            return collector.toReport();
        }

        validateSequenceNumbers(modalities, collector);
        validateGlobalCoverage(operation, modalities, collector);
        validateThreeCurrencyConsistency(operation, modalities, collector);

        PaymentModalityValidationContext context =
                new PaymentModalityValidationContext(
                        operation,
                        modalities,
                        workflowContext
                );

        for (TrPaymentModality modality : modalities) {
            validateGenericModalityFields(modality, context, collector);

            if (modality.getModalityType() != null) {
                registry.getStrategy(modality.getModalityType())
                        .validate(modality, context, collector);
            }
        }

        return collector.toReport();
    }

    public void validateOrThrow(
            MvtTrOperation operation,
            WorkflowCommandContext workflowContext
    ) {
        PaymentModalityValidationReport report = validate(operation, workflowContext);

        if (report.hasErrors()) {
            throw new PaymentModalityValidationException(report);
        }
    }

    private void validateOperationHeader(
            MvtTrOperation operation,
            PaymentModalityValidationCollector collector
    ) {
        if (operation == null) {
            collector.globalError(
                    "OPERATION_REQUIRED",
                    "operation",
                    "Operation is required to validate payment modalities."
            );
            return;
        }

        if (operation.getMntDevise() == null
                || operation.getMntDevise().compareTo(ZERO) <= 0) {
            collector.globalError(
                    "TRANSFER_AMOUNT_REQUIRED",
                    "mntDevise",
                    "Transfer amount must be provided and greater than zero."
            );
        }

        if (isBlank(operation.getCodeDevise())) {
            collector.globalError(
                    "TRANSFER_CURRENCY_REQUIRED",
                    "codeDevise",
                    "Transfer currency is required."
            );
        }
    }

    private void validateSequenceNumbers(
            List<TrPaymentModality> modalities,
            PaymentModalityValidationCollector collector
    ) {
        Set<Integer> sequenceNumbers = new HashSet<>();

        for (TrPaymentModality modality : modalities) {
            if (modality.getSequenceNo() == null) {
                collector.error(
                        modality,
                        "PAYMOD_SEQUENCE_REQUIRED",
                        "sequenceNo",
                        "Payment modality sequence number is required."
                );
                continue;
            }

            if (!sequenceNumbers.add(modality.getSequenceNo())) {
                collector.error(
                        modality,
                        "PAYMOD_SEQUENCE_DUPLICATED",
                        "sequenceNo",
                        "Payment modality sequence number must be unique inside the operation."
                );
            }
        }
    }
    private void validateThreeCurrencyConsistency(
            MvtTrOperation operation,
            List<TrPaymentModality> modalities,
            PaymentModalityValidationCollector collector
    ) {
        if (operation == null) {
            return;
        }

        for (TrPaymentModality modality : modalities) {
            if (modality == null) {
                continue;
            }

            if (isBlank(operation.getCodeDeviseOrdre())) {
                collector.globalWarning(
                        "ORDER_CURRENCY_MISSING",
                        "codeDeviseOrdre",
                        "Order currency is missing. The system should distinguish order currency, transfer currency and funding currency."
                );
            }

            if (isBlank(operation.getCodeDevise())) {
                collector.globalError(
                        "TRANSFER_CURRENCY_MISSING",
                        "codeDevise",
                        "Transfer currency is required."
                );
            }

            if (modality.getModalityType() == PaymentModalityType.TND_FX_PURCHASE_NORMAL
                    || modality.getModalityType() == PaymentModalityType.NEGOTIATED_FX_PURCHASE) {
                if (!"TND".equalsIgnoreCase(modality.getDebitAccountCurrency())) {
                    collector.error(
                            modality,
                            "FUNDING_CURRENCY_TND_REQUIRED",
                            "debitAccountCurrency",
                            "For FX purchase against TND, funding currency must be TND."
                    );
                }
            }

            if (modality.getModalityType() == PaymentModalityType.DIRECT_FOREIGN_CURRENCY_ACCOUNT_DEBIT) {
                if (!sameCurrency(
                        modality.getDebitAccountCurrency(),
                        operation.getCodeDevise()
                )) {
                    collector.error(
                            modality,
                            "FUNDING_CURRENCY_MUST_EQUAL_TRANSFER_CURRENCY",
                            "debitAccountCurrency",
                            "For direct foreign currency account debit, funding currency must equal transfer currency."
                    );
                }
            }

            if (modality.getModalityType() == PaymentModalityType.ARBITRAGE) {
                if (sameCurrency(
                        modality.getDebitAccountCurrency(),
                        operation.getCodeDevise()
                )) {
                    collector.error(
                            modality,
                            "ARBITRAGE_REQUIRES_DIFFERENT_CURRENCY",
                            "debitAccountCurrency",
                            "For arbitrage, funding currency must be different from transfer currency."
                    );
                }
            }
        }
    }

    private void validateGenericModalityFields(
            TrPaymentModality modality,
            PaymentModalityValidationContext context,
            PaymentModalityValidationCollector collector
    ) {
        if (modality == null) {
            collector.globalError(
                    "PAYMOD_NULL",
                    "paymentModalities",
                    "Payment modality cannot be null."
            );
            return;
        }

        if (modality.getModalityType() == null) {
            collector.error(
                    modality,
                    "PAYMOD_TYPE_REQUIRED",
                    "modalityType",
                    "Payment modality type is required."
            );
        }

        if (modality.getCoveredTransferAmount() == null
                || modality.getCoveredTransferAmount().compareTo(ZERO) <= 0) {
            collector.error(
                    modality,
                    "PAYMOD_COVERED_AMOUNT_REQUIRED",
                    "coveredTransferAmount",
                    "Covered transfer amount must be provided and greater than zero."
            );
        }

        if (isBlank(modality.getCoveredTransferCurrency())) {
            collector.error(
                    modality,
                    "PAYMOD_COVERED_CURRENCY_REQUIRED",
                    "coveredTransferCurrency",
                    "Covered transfer currency is required."
            );
        } else if (!sameCurrency(
                modality.getCoveredTransferCurrency(),
                context.transferCurrency()
        )) {
            collector.error(
                    modality,
                    "PAYMOD_COVERED_CURRENCY_MISMATCH",
                    "coveredTransferCurrency",
                    "Covered transfer currency must match operation transfer currency."
            );
        }

        if (modality.getCoveragePercentage() != null) {
            if (modality.getCoveragePercentage().compareTo(ZERO) <= 0
                    || modality.getCoveragePercentage().compareTo(HUNDRED) > 0) {
                collector.error(
                        modality,
                        "PAYMOD_PERCENTAGE_INVALID",
                        "coveragePercentage",
                        "Coverage percentage must be greater than 0 and less than or equal to 100."
                );
            }
        }
    }

    private void validateGlobalCoverage(
            MvtTrOperation operation,
            List<TrPaymentModality> modalities,
            PaymentModalityValidationCollector collector
    ) {
        if (operation == null || operation.getMntDevise() == null) {
            return;
        }

        BigDecimal totalCoveredAmount = modalities.stream()
                .map(TrPaymentModality::getCoveredTransferAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (!amountsEqual(totalCoveredAmount, operation.getMntDevise(), AMOUNT_TOLERANCE)) {
            collector.globalError(
                    "PAYMOD_TOTAL_AMOUNT_MISMATCH",
                    "paymentModalities.coveredTransferAmount",
                    "Sum of covered transfer amounts must be equal to the transfer amount."
            );
        }

        boolean allPercentagesProvided = modalities.stream()
                .allMatch(modality -> modality.getCoveragePercentage() != null);

        if (allPercentagesProvided) {
            BigDecimal totalPercentage = modalities.stream()
                    .map(TrPaymentModality::getCoveragePercentage)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (!amountsEqual(totalPercentage, HUNDRED, PERCENTAGE_TOLERANCE)) {
                collector.globalError(
                        "PAYMOD_TOTAL_PERCENTAGE_MISMATCH",
                        "paymentModalities.coveragePercentage",
                        "Sum of coverage percentages must be equal to 100."
                );
            }
        }
    }

    private boolean amountsEqual(
            BigDecimal left,
            BigDecimal right,
            BigDecimal tolerance
    ) {
        if (left == null || right == null) {
            return false;
        }

        return left.subtract(right).abs().compareTo(tolerance) <= 0;
    }

    private boolean sameCurrency(String left, String right) {
        if (left == null || right == null) {
            return false;
        }

        return left.trim().equalsIgnoreCase(right.trim());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
