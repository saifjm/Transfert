package com.smi.mstr.transfer.application.payment.strategy;

import com.smi.mstr.transfer.application.payment.PaymentResourceCommand;
import com.smi.mstr.transfer.application.payment.PaymentSecurityCommand;
import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.domain.entity.TrPaymentModality;
import com.smi.mstr.transfer.domain.enums.PaymentModalityType;

import java.math.BigDecimal;

public interface PaymentModalityHandler {

    PaymentModalityType supportedType();

    void validate(MvtTrOperation operation, TrPaymentModality modalityp);

    PaymentResourceCommand buildAvailabilityCommand(
            MvtTrOperation operation,
            TrPaymentModality modality
    );

    PaymentSecurityCommand buildSecurityCommand(
            MvtTrOperation operation,
            TrPaymentModality modality,
            BigDecimal estimatedFeesAmount,
            String estimatedFeesCurrency
    );
}