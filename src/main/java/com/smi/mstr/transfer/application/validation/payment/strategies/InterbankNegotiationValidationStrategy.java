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
public class InterbankNegotiationValidationStrategy
        extends AbstractPaymentModalityValidationStrategy
        implements PaymentModalityValidationStrategy {

    @Override
    public boolean supports(PaymentModalityType modalityType) {
        return modalityType == PaymentModalityType.INTERBANK_NEGOTIATION;
    }

    @Override
    public void validate(
            TrPaymentModality modality,
            PaymentModalityValidationContext context,
            PaymentModalityValidationCollector collector
    ) {
        requireResourceType(
                modality,
                PaymentResourceType.INTERBANK_DEAL,
                collector
        );

        requireResourceReference(
                modality,
                collector,
                "Interbank negotiation requires a negotiation or deal reference."
        );

        requireFxType(
                modality,
                FxType.INTERBANK,
                collector
        );

        requireFxRateOrReference(
                modality,
                collector
        );

        if (!isYes(modality.getFxRequired())) {
            collector.error(
                    modality,
                    "PAYMOD_INTERBANK_NEGOTIATION_FX_REQUIRED",
                    "fxRequired",
                    "FX is required for interbank currency negotiation."
            );
        }

        if (isBlank(modality.getCoveredTransferCurrency())) {
            collector.error(
                    modality,
                    "PAYMOD_INTERBANK_TARGET_CURRENCY_REQUIRED",
                    "coveredTransferCurrency",
                    "Obtained / covered currency is required for interbank negotiation."
            );
        }

        if (modality.getCoveredTransferAmount() == null
                || modality.getCoveredTransferAmount().compareTo(ZERO) <= 0) {
            collector.error(
                    modality,
                    "PAYMOD_INTERBANK_TARGET_AMOUNT_REQUIRED",
                    "coveredTransferAmount",
                    "Obtained / covered amount must be greater than zero for interbank negotiation."
            );
        }
    }
}
