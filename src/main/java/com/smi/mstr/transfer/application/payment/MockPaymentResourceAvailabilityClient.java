package com.smi.mstr.transfer.application.payment;

import com.smi.mstr.transfer.domain.enums.PaymentResourceAvailabilityStatus;
import com.smi.mstr.transfer.dto.payment.PaymentResourceAvailabilityItemDto;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConditionalOnProperty(
        name = "mstr.payment.resource-availability.client",
        havingValue = "mock",
        matchIfMissing = true
)
public class MockPaymentResourceAvailabilityClient implements PaymentResourceAvailabilityClient {

    @Override
    public PaymentResourceAvailabilityItemDto check(PaymentResourceCommand command) {
        if (command.resourceRef() == null || command.resourceRef().isBlank()) {
            return new PaymentResourceAvailabilityItemDto(
                    command.modalityId(),
                    command.resourceType(),
                    command.resourceRef(),
                    command.amountToCheck(),
                    command.currencyToCheck(),
                    BigDecimal.ZERO,
                    command.currencyToCheck(),
                    PaymentResourceAvailabilityStatus.ERROR,
                    "Payment resource reference is missing."
            );
        }

        if (command.resourceRef().endsWith("LOW")) {
            return new PaymentResourceAvailabilityItemDto(
                    command.modalityId(),
                    command.resourceType(),
                    command.resourceRef(),
                    command.amountToCheck(),
                    command.currencyToCheck(),
                    new BigDecimal("100.000"),
                    command.currencyToCheck(),
                    PaymentResourceAvailabilityStatus.INSUFFICIENT,
                    "Payment resource is insufficient."
            );
        }

        return new PaymentResourceAvailabilityItemDto(
                command.modalityId(),
                command.resourceType(),
                command.resourceRef(),
                command.amountToCheck(),
                command.currencyToCheck(),
                new BigDecimal("999999999.000"),
                command.currencyToCheck(),
                PaymentResourceAvailabilityStatus.AVAILABLE,
                "Payment resource is available."
        );
    }
}
