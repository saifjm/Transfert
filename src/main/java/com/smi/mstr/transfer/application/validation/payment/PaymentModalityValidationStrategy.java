package com.smi.mstr.transfer.application.validation.payment;

import com.smi.mstr.transfer.domain.entity.TrPaymentModality;
import com.smi.mstr.transfer.domain.enums.PaymentModalityType;

public interface PaymentModalityValidationStrategy {

    boolean supports(PaymentModalityType modalityType);

    void validate(
            TrPaymentModality modality,
            PaymentModalityValidationContext context,
            PaymentModalityValidationCollector collector
    );
}