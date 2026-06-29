package com.smi.mstr.transfer.application.validation.payment;

import com.smi.mstr.transfer.domain.enums.PaymentModalityType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentModalityValidationRegistry {

    private final Map<PaymentModalityType, PaymentModalityValidationStrategy> strategies;

    public PaymentModalityValidationRegistry(
            List<PaymentModalityValidationStrategy> strategyList
    ) {
        this.strategies = new EnumMap<>(PaymentModalityType.class);

        for (PaymentModalityType type : PaymentModalityType.values()) {
            for (PaymentModalityValidationStrategy strategy : strategyList) {
                if (strategy.supports(type)) {
                    strategies.put(type, strategy);
                    break;
                }
            }
        }
    }

    public PaymentModalityValidationStrategy getStrategy(PaymentModalityType modalityType) {
        PaymentModalityValidationStrategy strategy = strategies.get(modalityType);

        if (strategy == null) {
            throw new IllegalArgumentException(
                    "No payment modality validation strategy configured for type: "
                            + modalityType
            );
        }

        return strategy;
    }
}