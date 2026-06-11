package com.smi.mstr.transfer.application.payment;

import com.smi.mstr.transfer.domain.entity.TrPaymentModality;
import com.smi.mstr.transfer.domain.enums.PaymentModalityType;
import com.smi.mstr.transfer.domain.enums.PaymentResourceType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PaymentSecurityCalculationService {

    private final PaymentResourceResolver resourceResolver;

    public PaymentSecurityCalculationService(PaymentResourceResolver resourceResolver) {
        this.resourceResolver = resourceResolver;
    }

    public PaymentResourceCommand buildAvailabilityCommand(TrPaymentModality modality) {
        PaymentResourceType resourceType = resourceResolver.resolveResourceType(modality);
        String resourceRef = resourceResolver.resolveResourceRef(modality);

        BigDecimal amountToCheck = resolveAmountToSecure(modality, BigDecimal.ZERO);
        String currencyToCheck = resolveCurrencyToSecure(modality);

        return new PaymentResourceCommand(
                modality.getModalityId(),
                resourceType,
                resourceRef,
                amountToCheck,
                currencyToCheck,
                modality.getTargetAmount(),
                modality.getTargetCurrency()
        );
    }

    public PaymentSecurityCommand buildSecurityCommand(
            TrPaymentModality modality,
            BigDecimal estimatedFeesAmount,
            String estimatedFeesCurrency
    ) {
        PaymentResourceType resourceType = resourceResolver.resolveResourceType(modality);
        String resourceRef = resourceResolver.resolveResourceRef(modality);

        BigDecimal fees = estimatedFeesAmount == null ? BigDecimal.ZERO : estimatedFeesAmount;

        BigDecimal counterValueAmount = calculateCounterValue(modality);
        String counterValueCurrency = resolveCounterValueCurrency(modality);

        BigDecimal amountToSecure = resolveAmountToSecure(modality, fees);
        String currencyToSecure = resolveCurrencyToSecure(modality);

        return new PaymentSecurityCommand(
                modality.getModalityId(),
                resourceType,
                resourceRef,

                modality.getTargetAmount(),
                modality.getTargetCurrency(),

                modality.getFxRate(),
                counterValueAmount,
                counterValueCurrency,

                estimatedFeesAmount,
                estimatedFeesCurrency,

                amountToSecure,
                currencyToSecure
        );
    }

    private BigDecimal resolveAmountToSecure(
            TrPaymentModality modality,
            BigDecimal estimatedFeesAmount
    ) {
        if (requiresCounterValue(modality)) {
            BigDecimal counterValue = calculateCounterValue(modality);
            if (counterValue == null) {
                return modality.getTargetAmount();
            }
            return counterValue.add(estimatedFeesAmount);
        }

        if (modality.getSourceAmount() != null) {
            return modality.getSourceAmount().add(estimatedFeesAmount);
        }

        return modality.getTargetAmount().add(estimatedFeesAmount);
    }

    private String resolveCurrencyToSecure(TrPaymentModality modality) {
        if (requiresCounterValue(modality) && modality.getDebitAccountCurrency() != null) {
            return modality.getDebitAccountCurrency();
        }

        if (modality.getSourceCurrency() != null) {
            return modality.getSourceCurrency();
        }

        return modality.getTargetCurrency();
    }

    private BigDecimal calculateCounterValue(TrPaymentModality modality) {
        if (!requiresCounterValue(modality)) {
            return null;
        }

        if (modality.getTargetAmount() == null || modality.getFxRate() == null) {
            return null;
        }

        return modality.getTargetAmount()
                .multiply(modality.getFxRate())
                .setScale(3, RoundingMode.HALF_UP);
    }

    private String resolveCounterValueCurrency(TrPaymentModality modality) {
        if (!requiresCounterValue(modality)) {
            return null;
        }

        return modality.getDebitAccountCurrency();
    }

    private boolean requiresCounterValue(TrPaymentModality modality) {
        return modality.getModalityType() == PaymentModalityType.TND_FX_PURCHASE_NORMAL
                || modality.getModalityType() == PaymentModalityType.TND_FX_PURCHASE_NEGOTIATED;
    }
}
