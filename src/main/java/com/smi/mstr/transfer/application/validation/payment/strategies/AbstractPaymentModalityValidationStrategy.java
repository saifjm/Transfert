package com.smi.mstr.transfer.application.validation.payment.strategies;

import com.smi.mstr.transfer.application.validation.payment.PaymentModalityValidationCollector;
import com.smi.mstr.transfer.domain.entity.TrPaymentModality;
import com.smi.mstr.transfer.domain.enums.FxType;
import com.smi.mstr.transfer.domain.enums.PaymentResourceType;

import java.math.BigDecimal;

public abstract class AbstractPaymentModalityValidationStrategy {

    protected static final BigDecimal ZERO = BigDecimal.ZERO;
    protected static final BigDecimal AMOUNT_TOLERANCE = new BigDecimal("0.001");
    protected static final BigDecimal TND_AMOUNT_TOLERANCE = new BigDecimal("5.000");

    protected void requireDebitAccount(
            TrPaymentModality modality,
            PaymentModalityValidationCollector collector
    ) {
        if (isBlank(modality.getDebitAccountNumber())) {
            collector.error(
                    modality,
                    "PAYMOD_DEBIT_ACCOUNT_REQUIRED",
                    "debitAccountNumber",
                    "Debit account is required for this payment modality."
            );
        }
    }

    protected void requireDebitCurrency(
            TrPaymentModality modality,
            PaymentModalityValidationCollector collector
    ) {
        if (isBlank(modality.getDebitAccountCurrency())) {
            collector.error(
                    modality,
                    "PAYMOD_DEBIT_CURRENCY_REQUIRED",
                    "debitAccountCurrency",
                    "Debit account currency is required for this payment modality."
            );
        }
    }

    protected void requireDebitAmount(
            TrPaymentModality modality,
            PaymentModalityValidationCollector collector
    ) {
        if (modality.getDebitAmount() == null
                || modality.getDebitAmount().compareTo(ZERO) <= 0) {
            collector.error(
                    modality,
                    "PAYMOD_DEBIT_AMOUNT_REQUIRED",
                    "debitAmount",
                    "Debit amount must be provided and greater than zero."
            );
        }
    }

    protected void requireFxRateOrReference(
            TrPaymentModality modality,
            PaymentModalityValidationCollector collector
    ) {
        if (modality.getFxRate() == null && isBlank(modality.getFxReference())) {
            collector.error(
                    modality,
                    "PAYMOD_FX_RATE_OR_REFERENCE_REQUIRED",
                    "fxRate",
                    "FX rate or FX reference is required for this payment modality."
            );
        }

        if (modality.getFxRate() != null
                && modality.getFxRate().compareTo(ZERO) <= 0) {
            collector.error(
                    modality,
                    "PAYMOD_FX_RATE_INVALID",
                    "fxRate",
                    "FX rate must be greater than zero."
            );
        }
    }

    protected void requireResourceReference(
            TrPaymentModality modality,
            PaymentModalityValidationCollector collector,
            String message
    ) {
        if (isBlank(modality.getResourceReference())) {
            collector.error(
                    modality,
                    "PAYMOD_RESOURCE_REFERENCE_REQUIRED",
                    "resourceReference",
                    message
            );
        }
    }

    protected void requireResourceType(
            TrPaymentModality modality,
            PaymentResourceType expected,
            PaymentModalityValidationCollector collector
    ) {
        if (modality.getResourceType() != null
                && modality.getResourceType() != expected) {
            collector.error(
                    modality,
                    "PAYMOD_RESOURCE_TYPE_INVALID",
                    "resourceType",
                    "Invalid resource type. Expected: " + expected
            );
        }
    }

    protected void requireFxType(
            TrPaymentModality modality,
            FxType expected,
            PaymentModalityValidationCollector collector
    ) {
        if (modality.getFxType() != null
                && modality.getFxType() != expected) {
            collector.error(
                    modality,
                    "PAYMOD_FX_TYPE_INVALID",
                    "fxType",
                    "Invalid FX type. Expected: " + expected
            );
        }
    }

    protected void requireNoFx(
            TrPaymentModality modality,
            PaymentModalityValidationCollector collector
    ) {
        if (isYes(modality.getFxRequired())) {
            collector.error(
                    modality,
                    "PAYMOD_FX_NOT_ALLOWED",
                    "fxRequired",
                    "FX must not be required for this payment modality."
            );
        }

        if (modality.getFxType() != null
                && modality.getFxType() != FxType.NOT_REQUIRED) {
            collector.error(
                    modality,
                    "PAYMOD_FX_TYPE_NOT_ALLOWED",
                    "fxType",
                    "FX type must be NOT_REQUIRED for this payment modality."
            );
        }
    }

    protected void requireCurrencyEquals(
            TrPaymentModality modality,
            String actualCurrency,
            String expectedCurrency,
            String field,
            PaymentModalityValidationCollector collector
    ) {
        if (isBlank(actualCurrency)) {
            return;
        }

        if (!actualCurrency.trim().equalsIgnoreCase(expectedCurrency)) {
            collector.error(
                    modality,
                    "PAYMOD_CURRENCY_INVALID",
                    field,
                    "Currency must be " + expectedCurrency + "."
            );
        }
    }

    protected void requireCurrencySameAsCoveredCurrency(
            TrPaymentModality modality,
            PaymentModalityValidationCollector collector
    ) {
        if (isBlank(modality.getDebitAccountCurrency())
                || isBlank(modality.getCoveredTransferCurrency())) {
            return;
        }

        if (!modality.getDebitAccountCurrency()
                .trim()
                .equalsIgnoreCase(modality.getCoveredTransferCurrency().trim())) {
            collector.error(
                    modality,
                    "PAYMOD_DEBIT_CURRENCY_MISMATCH",
                    "debitAccountCurrency",
                    "Debit account currency must match covered transfer currency."
            );
        }
    }

    protected void requireCurrencyDifferentFromCoveredCurrency(
            TrPaymentModality modality,
            PaymentModalityValidationCollector collector
    ) {
        if (isBlank(modality.getDebitAccountCurrency())
                || isBlank(modality.getCoveredTransferCurrency())) {
            return;
        }

        if (modality.getDebitAccountCurrency()
                .trim()
                .equalsIgnoreCase(modality.getCoveredTransferCurrency().trim())) {
            collector.error(
                    modality,
                    "PAYMOD_DEBIT_CURRENCY_MUST_DIFFER",
                    "debitAccountCurrency",
                    "Debit account currency must be different from covered transfer currency."
            );
        }
    }

    protected boolean isYes(String value) {
        return "Y".equalsIgnoreCase(value)
                || "O".equalsIgnoreCase(value)
                || "YES".equalsIgnoreCase(value)
                || "TRUE".equalsIgnoreCase(value);
    }

    protected boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}