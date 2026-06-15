package com.smi.mstr.transfer.application.payment;

import com.smi.mstr.transfer.application.payment.strategy.PaymentModalityHandlerRegistry;
import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.domain.entity.TrPaymentModality;
import com.smi.mstr.transfer.dto.payment.PaymentModalityDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class PaymentModalityValidator {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100.00");

    private final PaymentModalityHandlerRegistry handlerRegistry;

    public PaymentModalityValidator(PaymentModalityHandlerRegistry handlerRegistry) {
        this.handlerRegistry = handlerRegistry;
    }

    public void validateDtoList(List<PaymentModalityDto> modalities) {
        if (modalities == null || modalities.isEmpty()) {
            throw new IllegalArgumentException("At least one payment modality is required.");
        }

        BigDecimal totalPercent = BigDecimal.ZERO;

        for (PaymentModalityDto modality : modalities) {
            if (modality.modalityType() == null) {
                throw new IllegalArgumentException("Payment modality type is required.");
            }

            if (modality.sharePercent() == null
                    || modality.sharePercent().compareTo(BigDecimal.ZERO) <= 0
                    || modality.sharePercent().compareTo(ONE_HUNDRED) > 0) {
                throw new IllegalArgumentException(
                        "Payment modality percentage must be greater than 0 and less than or equal to 100."
                );
            }

            totalPercent = totalPercent.add(modality.sharePercent());
        }

        if (totalPercent.compareTo(ONE_HUNDRED) != 0) {
            throw new IllegalArgumentException(
                    "The total payment modality percentage must be equal to 100%. Current total: "
                            + totalPercent + "%"
            );
        }
    }

    public void validateEntity(
            MvtTrOperation operation,
            TrPaymentModality modality
    ) {
        handlerRegistry
                .getHandler(modality.getModalityType())
                .validate(operation, modality);
    }
}