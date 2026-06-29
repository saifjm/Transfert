package com.smi.mstr.transfer.application.validation.payment.strategies;

import com.smi.mstr.transfer.application.validation.payment.PaymentModalityValidationCollector;
import com.smi.mstr.transfer.application.validation.payment.PaymentModalityValidationContext;
import com.smi.mstr.transfer.application.validation.payment.PaymentModalityValidationStrategy;
import com.smi.mstr.transfer.domain.entity.TrPaymentModality;
import com.smi.mstr.transfer.domain.enums.FxType;
import com.smi.mstr.transfer.domain.enums.PaymentModalityType;
import com.smi.mstr.transfer.domain.enums.PaymentResourceType;
import org.springframework.stereotype.Component;

@Component
public class ArbitrageValidationStrategy
        extends AbstractPaymentModalityValidationStrategy
        implements PaymentModalityValidationStrategy {

    @Override
    public boolean supports(PaymentModalityType modalityType) {
        return modalityType == PaymentModalityType.ARBITRAGE;
    }

    @Override
    public void validate(
            TrPaymentModality modality,
            PaymentModalityValidationContext context,
            PaymentModalityValidationCollector collector
    ) {
        requireResourceType(modality, PaymentResourceType.ACCOUNT, collector);
        requireDebitAccount(modality, collector);
        requireDebitCurrency(modality, collector);
        requireDebitAmount(modality, collector);

        requireCurrencyDifferentFromCoveredCurrency(modality, collector);

        requireFxType(modality, FxType.ARBITRAGE, collector);
        requireFxRateOrReference(modality, collector);

        if (!isYes(modality.getFxRequired())) {
            collector.error(
                    modality,
                    "PAYMOD_ARBITRAGE_FX_REQUIRED",
                    "fxRequired",
                    "FX is required for currency arbitrage."
            );
        }
    }
}