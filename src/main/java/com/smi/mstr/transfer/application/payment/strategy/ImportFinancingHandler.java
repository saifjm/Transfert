package com.smi.mstr.transfer.application.payment.strategy;

import com.smi.mstr.transfer.application.payment.PaymentResourceCommand;
import com.smi.mstr.transfer.application.payment.PaymentSecurityCommand;
import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.domain.entity.TrPaymentModality;
import com.smi.mstr.transfer.domain.enums.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ImportFinancingHandler implements PaymentModalityHandler {

    @Override
    public PaymentModalityType supportedType() {
        return PaymentModalityType.IMPORT_FINANCING;
    }

    @Override
    public void validate(MvtTrOperation operation, TrPaymentModality modality) {
        require(modality.getFinancingRef(), "Financing folder reference is required.");
        require(modality.getTargetAmount(), "Target amount is required.");
        require(modality.getTargetCurrency(), "Target currency is required.");
    }

    @Override
    public PaymentResourceCommand buildAvailabilityCommand(
            MvtTrOperation operation,
            TrPaymentModality modality
    ) {
        return new PaymentResourceCommand(
                modality.getModalityId(),
                PaymentResourceType.FINANCING_LINE,
                PaymentImpactTarget.FINANCING_FOLDER,
                PaymentImpactAction.RESERVE_FINANCING_AMOUNT,
                modality.getFinancingRef(),

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
        return new PaymentSecurityCommand(
                modality.getModalityId(),
                PaymentResourceType.FINANCING_LINE,
                PaymentImpactTarget.FINANCING_FOLDER,
                PaymentImpactAction.RESERVE_FINANCING_AMOUNT,
                modality.getFinancingRef(),

                modality.getTargetAmount(),
                modality.getTargetCurrency(),

                null,
                null,
                null,

                estimatedFeesAmount,
                estimatedFeesCurrency,

                modality.getTargetAmount(),
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