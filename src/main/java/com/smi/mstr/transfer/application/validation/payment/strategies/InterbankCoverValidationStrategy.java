package com.smi.mstr.transfer.application.validation.payment.strategies;

import com.smi.mstr.transfer.application.validation.payment.PaymentModalityValidationCollector;
import com.smi.mstr.transfer.application.validation.payment.PaymentModalityValidationContext;
import com.smi.mstr.transfer.application.validation.payment.PaymentModalityValidationStrategy;
import com.smi.mstr.transfer.domain.entity.TrPaymentModality;
import com.smi.mstr.transfer.domain.enums.InterbankRouteType;
import com.smi.mstr.transfer.domain.enums.PaymentModalityType;
import com.smi.mstr.transfer.domain.enums.PaymentResourceType;
import org.springframework.stereotype.Component;

@Component
public class InterbankCoverValidationStrategy
        extends AbstractPaymentModalityValidationStrategy
        implements PaymentModalityValidationStrategy {

    @Override
    public boolean supports(PaymentModalityType modalityType) {
        return modalityType == PaymentModalityType.INTERBANK_COVER;
    }

    @Override
    public void validate(
            TrPaymentModality modality,
            PaymentModalityValidationContext context,
            PaymentModalityValidationCollector collector
    ) {
        requireResourceType(modality, PaymentResourceType.INTERBANK_COVER, collector);
        requireResourceReference(
                modality,
                collector,
                "Interbank cover modality requires cover reference."
        );

        if (context.operation() != null
                && context.operation().getRouteType() != null
                && context.operation().getRouteType() != InterbankRouteType.COVER_REQUIRED) {
            collector.warning(
                    modality,
                    "PAYMOD_INTERBANK_COVER_ROUTE_WARNING",
                    "routeType",
                    "Interbank cover modality is used while operation route type is not COVER_REQUIRED."
            );
        }
    }
}
