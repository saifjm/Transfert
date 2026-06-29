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
public class NegotiatedFxPurchaseValidationStrategy
        extends AbstractPaymentModalityValidationStrategy
        implements PaymentModalityValidationStrategy {

    @Override
    public boolean supports(PaymentModalityType modalityType) {
        return modalityType == PaymentModalityType.NEGOTIATED_FX_PURCHASE;
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

        requireCurrencyEquals(
                modality,
                modality.getDebitAccountCurrency(),
                "TND",
                "debitAccountCurrency",
                collector
        );

        requireFxType(modality, FxType.NEGOTIATED, collector);
        requireFxRateOrReference(modality, collector);

        if (isBlank(modality.getFxReference())) {
            collector.warning(
                    modality,
                    "PAYMOD_NEGOTIATED_FX_REF_RECOMMENDED",
                    "fxReference",
                    "Negotiated FX purchase should normally reference an FX deal."
            );
        }
    }
}
