package com.smi.mstr.transfer.application.payment.strategy;

import com.smi.mstr.transfer.application.payment.PaymentResourceCommand;
import com.smi.mstr.transfer.application.payment.PaymentSecurityCommand;
import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.domain.entity.TrPaymentModality;
import com.smi.mstr.transfer.domain.enums.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class TndFxPurchaseNormalHandler implements PaymentModalityHandler {

    @Override
    public PaymentModalityType supportedType() {
        return PaymentModalityType.TND_FX_PURCHASE_NORMAL;
    }

    @Override
    public void validate(MvtTrOperation operation, TrPaymentModality modality) {
        require(modality.getDebitAccountRef(), "Debit account reference is required.");
        require(modality.getDebitAccountCurrency(), "Debit account currency is required.");
        require(modality.getTargetAmount(), "Target amount is required.");
        require(modality.getTargetCurrency(), "Target currency is required.");
        require(modality.getFxRate(), "FX rate is required.");

        if (!"TND".equalsIgnoreCase(modality.getDebitAccountCurrency())) {
            throw new IllegalArgumentException(
                    "Debit account currency must be TND for normal TND FX purchase."
            );
        }
    }

    @Override
    public PaymentResourceCommand buildAvailabilityCommand(
            MvtTrOperation operation,
            TrPaymentModality modality
    ) {
        BigDecimal counterValue = calculateCounterValue(modality);

        return new PaymentResourceCommand(
                modality.getModalityId(),
                PaymentResourceType.ACCOUNT_BALANCE,
                PaymentImpactTarget.ACCOUNT,
                PaymentImpactAction.BLOCK_AMOUNT,
                modality.getDebitAccountRef(),

                counterValue,
                modality.getDebitAccountCurrency(),

                modality.getTargetAmount(),
                modality.getTargetCurrency(),

                modality.getFxRate(),
                counterValue,
                modality.getDebitAccountCurrency()
        );
    }

    @Override
    public PaymentSecurityCommand buildSecurityCommand(
            MvtTrOperation operation,
            TrPaymentModality modality,
            BigDecimal estimatedFeesAmount,
            String estimatedFeesCurrency
    ) {
        BigDecimal counterValue = calculateCounterValue(modality);
        BigDecimal fees = estimatedFeesAmount == null ? BigDecimal.ZERO : estimatedFeesAmount;
        BigDecimal amountToSecure = counterValue.add(fees);

        return new PaymentSecurityCommand(
                modality.getModalityId(),
                PaymentResourceType.ACCOUNT_BALANCE,
                PaymentImpactTarget.ACCOUNT,
                PaymentImpactAction.BLOCK_AMOUNT,
                modality.getDebitAccountRef(),

                modality.getTargetAmount(),
                modality.getTargetCurrency(),

                modality.getFxRate(),
                counterValue,
                modality.getDebitAccountCurrency(),

                estimatedFeesAmount,
                estimatedFeesCurrency,

                amountToSecure,
                modality.getDebitAccountCurrency()
        );
    }

    private BigDecimal calculateCounterValue(TrPaymentModality modality) {
        return modality.getTargetAmount()
                .multiply(modality.getFxRate())
                .setScale(3, RoundingMode.HALF_UP);
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
