package com.smi.mstr.transfer.application.payment.strategy;

import com.smi.mstr.transfer.application.payment.PaymentResourceCommand;
import com.smi.mstr.transfer.application.payment.PaymentSecurityCommand;
import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.domain.entity.TrPaymentModality;
import com.smi.mstr.transfer.domain.enums.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DirectForeignCurrencyAccountDebitHandler implements PaymentModalityHandler {

    @Override
    public PaymentModalityType supportedType() {
        return PaymentModalityType.DIRECT_FOREIGN_CURRENCY_ACCOUNT_DEBIT;
    }

    @Override
    public void validate(MvtTrOperation operation, TrPaymentModality modality) {
        require(modality.getDebitAccountRef(), "Debit account reference is required.");
        require(modality.getDebitAccountCurrency(), "Debit account currency is required.");
        require(modality.getTargetAmount(), "Target amount is required.");
        require(modality.getTargetCurrency(), "Target currency is required.");

        if (!modality.getDebitAccountCurrency().equalsIgnoreCase(modality.getTargetCurrency())) {
            throw new IllegalArgumentException(
                    "Debit account currency must be equal to transfer currency for direct foreign currency debit."
            );
        }
    }

    @Override
    public PaymentResourceCommand buildAvailabilityCommand(
            MvtTrOperation operation,
            TrPaymentModality modality
    ) {
        return new PaymentResourceCommand(
                modality.getModalityId(),
                PaymentResourceType.ACCOUNT_BALANCE,
                PaymentImpactTarget.ACCOUNT,
                PaymentImpactAction.BLOCK_AMOUNT,
                modality.getDebitAccountRef(),

                modality.getTargetAmount(),
                modality.getTargetCurrency(),

                modality.getTargetAmount(),
                modality.getTargetCurrency(),

                null,
                null,
                null
        );
    }

    @Override
    public PaymentSecurityCommand buildSecurityCommand(
            MvtTrOperation operation,
            TrPaymentModality modality,
            BigDecimal estimatedFeesAmount,
            String estimatedFeesCurrency
    ) {
        BigDecimal fees = estimatedFeesAmount == null ? BigDecimal.ZERO : estimatedFeesAmount;

        /*
         * Simple version:
         * If fees are in the same currency as the debit account, add them.
         * Otherwise, do not add them here. Later, fees should have their own modality
         * or a dedicated charges debit rule.
         */
        BigDecimal amountToSecure = modality.getTargetAmount();

        if (estimatedFeesCurrency != null
                && estimatedFeesCurrency.equalsIgnoreCase(modality.getTargetCurrency())) {
            amountToSecure = amountToSecure.add(fees);
        }

        return new PaymentSecurityCommand(
                modality.getModalityId(),
                PaymentResourceType.ACCOUNT_BALANCE,
                PaymentImpactTarget.ACCOUNT,
                PaymentImpactAction.BLOCK_AMOUNT,
                modality.getDebitAccountRef(),

                modality.getTargetAmount(),
                modality.getTargetCurrency(),

                null,
                null,
                null,

                estimatedFeesAmount,
                estimatedFeesCurrency,

                amountToSecure,
                modality.getTargetCurrency()
        );
    }

    private void require(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }

        if (value instanceof String s && s.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}