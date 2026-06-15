package com.smi.mstr.transfer.application.payment;

import com.smi.mstr.transfer.domain.entity.TrPaymentModality;
import com.smi.mstr.transfer.domain.enums.PaymentImpactAction;
import com.smi.mstr.transfer.domain.enums.PaymentImpactTarget;
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
        PaymentImpactTarget impactTarget = resolveImpactTarget(modality);
        PaymentImpactAction impactAction = resolveImpactAction(modality);
        String resourceRef = resourceResolver.resolveResourceRef(modality);

        BigDecimal counterValueAmount = calculateCounterValue(modality);
        String counterValueCurrency = resolveCounterValueCurrency(modality);

        BigDecimal amountToCheck = resolveAmountToSecure(modality, BigDecimal.ZERO);
        String currencyToCheck = resolveCurrencyToSecure(modality);

        return new PaymentResourceCommand(
                modality.getModalityId(),

                resourceType,
                impactTarget,
                impactAction,

                resourceRef,

                amountToCheck,
                currencyToCheck,

                modality.getTargetAmount(),
                modality.getTargetCurrency(),

                modality.getFxRate(),
                counterValueAmount,
                counterValueCurrency
        );
    }

    public PaymentSecurityCommand buildSecurityCommand(
            TrPaymentModality modality,
            BigDecimal estimatedFeesAmount,
            String estimatedFeesCurrency
    ) {
        PaymentResourceType resourceType = resourceResolver.resolveResourceType(modality);
        PaymentImpactTarget impactTarget = resolveImpactTarget(modality);
        PaymentImpactAction impactAction = resolveImpactAction(modality);
        String resourceRef = resourceResolver.resolveResourceRef(modality);

        BigDecimal fees = estimatedFeesAmount == null
                ? BigDecimal.ZERO
                : estimatedFeesAmount;

        BigDecimal counterValueAmount = calculateCounterValue(modality);
        String counterValueCurrency = resolveCounterValueCurrency(modality);

        BigDecimal amountToSecure = resolveAmountToSecure(modality, fees);
        String currencyToSecure = resolveCurrencyToSecure(modality);

        return new PaymentSecurityCommand(
                modality.getModalityId(),

                resourceType,
                impactTarget,
                impactAction,

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

    private PaymentImpactTarget resolveImpactTarget(TrPaymentModality modality) {
        return switch (modality.getModalityType()) {
            case TND_FX_PURCHASE_NORMAL,
                 TND_FX_PURCHASE_NEGOTIATED,
                 DIRECT_FOREIGN_CURRENCY_ACCOUNT_DEBIT,
                 CURRENCY_ARBITRAGE -> PaymentImpactTarget.ACCOUNT;

            case FORWARD_FX_CONTRACT -> PaymentImpactTarget.FORWARD_CONTRACT;

            case IMPORT_FINANCING -> PaymentImpactTarget.FINANCING_FOLDER;

            case FUNDS_RECEIVED_LOCAL_BANK -> PaymentImpactTarget.RECEIVED_FUNDS;

            case INTERBANK_FX_COVER -> PaymentImpactTarget.INTERBANK_COVER;

            case OTHER -> PaymentImpactTarget.ACCOUNT;
        };
    }

    private PaymentImpactAction resolveImpactAction(TrPaymentModality modality) {
        return switch (modality.getModalityType()) {
            case TND_FX_PURCHASE_NORMAL,
                 DIRECT_FOREIGN_CURRENCY_ACCOUNT_DEBIT,
                 CURRENCY_ARBITRAGE -> PaymentImpactAction.BLOCK_AMOUNT;

            case TND_FX_PURCHASE_NEGOTIATED -> PaymentImpactAction.BLOCK_AMOUNT;

            case FORWARD_FX_CONTRACT -> PaymentImpactAction.RESERVE_CONTRACT;

            case IMPORT_FINANCING -> PaymentImpactAction.RESERVE_FINANCING_AMOUNT;

            case FUNDS_RECEIVED_LOCAL_BANK -> PaymentImpactAction.RESERVE_RECEIVED_FUNDS;

            case INTERBANK_FX_COVER -> PaymentImpactAction.RESERVE_INTERBANK_COVER;

            case OTHER -> PaymentImpactAction.BLOCK_AMOUNT;
        };
    }

    private BigDecimal resolveAmountToSecure(
            TrPaymentModality modality,
            BigDecimal estimatedFeesAmount
    ) {
        if (requiresCounterValue(modality)) {
            BigDecimal counterValue = calculateCounterValue(modality);

            if (counterValue == null) {
                return safeAmount(modality.getTargetAmount()).add(estimatedFeesAmount);
            }

            return counterValue.add(estimatedFeesAmount);
        }

        if (modality.getSourceAmount() != null) {
            return modality.getSourceAmount().add(estimatedFeesAmount);
        }

        return safeAmount(modality.getTargetAmount()).add(estimatedFeesAmount);
    }

    private String resolveCurrencyToSecure(TrPaymentModality modality) {
        if (requiresCounterValue(modality)
                && modality.getDebitAccountCurrency() != null) {
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

    private BigDecimal safeAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }
}