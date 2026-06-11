package com.smi.mstr.transfer.application.payment;

import com.smi.mstr.transfer.domain.enums.PaymentSecurityStatus;
import com.smi.mstr.transfer.dto.payment.PaymentSecurityItemDto;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@ConditionalOnProperty(
        name = "mstr.payment.security.client",
        havingValue = "mock",
        matchIfMissing = true
)
public class MockPaymentSecurityClient implements PaymentSecurityClient {

    @Override
    public PaymentSecurityItemDto secure(PaymentSecurityCommand command) {
        if (command.resourceRef() == null || command.resourceRef().isBlank()) {
            return new PaymentSecurityItemDto(
                    null,
                    command.modalityId(),
                    command.resourceType(),
                    PaymentSecurityStatus.FAILED,
                    command.resourceRef(),
                    command.requestedAmount(),
                    command.requestedCurrency(),
                    command.fxRate(),
                    command.counterValueAmount(),
                    command.counterValueCurrency(),
                    command.estimatedFeesAmount(),
                    command.estimatedFeesCurrency(),
                    null,
                    null,
                    null,
                    null,
                    "Payment resource reference is missing."
            );
        }

        String reference = securityPrefix(command) + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return new PaymentSecurityItemDto(
                null,
                command.modalityId(),
                command.resourceType(),
                PaymentSecurityStatus.SECURED,
                command.resourceRef(),
                command.requestedAmount(),
                command.requestedCurrency(),
                command.fxRate(),
                command.counterValueAmount(),
                command.counterValueCurrency(),
                command.estimatedFeesAmount(),
                command.estimatedFeesCurrency(),
                command.amountToSecure(),
                command.currencyToSecure(),
                reference,
                LocalDateTime.now(),
                "Payment resource secured successfully."
        );
    }

    private String securityPrefix(PaymentSecurityCommand command) {
        return switch (command.resourceType()) {
            case ACCOUNT_BALANCE -> "BLK";
            case FX_DEAL -> "FXRES";
            case FORWARD_CONTRACT -> "FWDRES";
            case FINANCING_LINE -> "FINRES";
            case RECEIVED_FUNDS -> "RCVRES";
            case INTERBANK_COVER -> "COVRES";
        };
    }
}
