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
public class ForwardFxValidationStrategy
        extends AbstractPaymentModalityValidationStrategy
        implements PaymentModalityValidationStrategy {

    @Override
    public boolean supports(PaymentModalityType modalityType) {
        return modalityType == PaymentModalityType.FORWARD_FX;
    }

    @Override
    public void validate(
            TrPaymentModality modality,
            PaymentModalityValidationContext context,
            PaymentModalityValidationCollector collector
    ) {
        requireResourceType(modality, PaymentResourceType.FX_CONTRACT, collector);
        requireFxType(modality, FxType.FORWARD, collector);

        requireResourceReference(
                modality,
                collector,
                "Forward FX modality requires a forward contract reference."
        );

        requireFxRateOrReference(modality, collector);
    }
}
