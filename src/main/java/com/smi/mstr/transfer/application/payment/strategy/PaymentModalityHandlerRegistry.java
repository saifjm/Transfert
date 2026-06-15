package com.smi.mstr.transfer.application.payment.strategy;

import com.smi.mstr.transfer.domain.enums.PaymentModalityType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentModalityHandlerRegistry {

    private final Map<PaymentModalityType, PaymentModalityHandler> handlers =
            new EnumMap<>(PaymentModalityType.class);

    public PaymentModalityHandlerRegistry(List<PaymentModalityHandler> handlerList) {
        for (PaymentModalityHandler handler : handlerList) {
            handlers.put(handler.supportedType(), handler);
        }
    }

    public PaymentModalityHandler getHandler(PaymentModalityType modalityType) {
        PaymentModalityHandler handler = handlers.get(modalityType);

        if (handler == null) {
            throw new IllegalArgumentException(
                    "No payment modality handler found for type: " + modalityType
            );
        }

        return handler;
    }
}