package com.smi.mstr.transfer.application.validation.payment.strategies;

import com.smi.mstr.transfer.application.validation.payment.PaymentModalityValidationCollector;
import com.smi.mstr.transfer.application.validation.payment.PaymentModalityValidationContext;
import com.smi.mstr.transfer.application.validation.payment.PaymentModalityValidationStrategy;
import com.smi.mstr.transfer.domain.entity.TrPaymentModality;
import com.smi.mstr.transfer.domain.enums.PaymentModalityType;
import org.springframework.stereotype.Component;

@Component
public class ManualExceptionValidationStrategy
        extends AbstractPaymentModalityValidationStrategy
        implements PaymentModalityValidationStrategy {

    @Override
    public boolean supports(PaymentModalityType modalityType) {
        return modalityType == PaymentModalityType.MANUAL_EXCEPTION;
    }

    @Override
    public void validate(
            TrPaymentModality modality,
            PaymentModalityValidationContext context,
            PaymentModalityValidationCollector collector
    ) {
        requireResourceReference(
                modality,
                collector,
                "Manual exception modality requires a manual justification reference."
        );

        collector.warning(
                modality,
                "PAYMOD_MANUAL_EXCEPTION_REVIEW",
                "modalityType",
                "Manual exception modality must be reviewed by back-office or supervisor."
        );
    }
}