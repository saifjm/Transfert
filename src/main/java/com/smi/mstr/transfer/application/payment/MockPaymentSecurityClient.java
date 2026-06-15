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
            return failed(command, "Payment resource reference is missing.");
        }

        if (command.resourceRef().endsWith("SECERR")) {
            return failed(command, "Mock security failure for payment resource.");
        }

        String reference = securityPrefix(command) + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return new PaymentSecurityItemDto(
                null,
                command.modalityId(),
                command.resourceType(),
                PaymentSecurityStatus.SECURED,
                command.resourceRef(),
                command.requestedTransferAmount(),
                command.requestedTransferCurrency(),
                command.fxRate(),
                command.counterValueAmount(),
                command.counterValueCurrency(),
                command.estimatedFeesAmount(),
                command.estimatedFeesCurrency(),
                command.amountToSecure(),
                command.currencyToSecure(),
                reference,
                LocalDateTime.now(),
                successMessage(command)
        );
    }

    private PaymentSecurityItemDto failed(
            PaymentSecurityCommand command,
            String message
    ) {
        return new PaymentSecurityItemDto(
                null,
                command.modalityId(),
                command.resourceType(),
                PaymentSecurityStatus.FAILED,
                command.resourceRef(),
                command.requestedTransferAmount(),
                command.requestedTransferCurrency(),
                command.fxRate(),
                command.counterValueAmount(),
                command.counterValueCurrency(),
                command.estimatedFeesAmount(),
                command.estimatedFeesCurrency(),
                null,
                null,
                null,
                null,
                message
        );
    }

    private String securityPrefix(PaymentSecurityCommand command) {
        return switch (command.impactTarget()) {
            case ACCOUNT -> "BLK";
            case FX_DEAL -> "FXRES";
            case FORWARD_CONTRACT -> "FWDRES";
            case FINANCING_FOLDER -> "FINRES";
            case RECEIVED_FUNDS -> "RCVRES";
            case INTERBANK_COVER -> "COVRES";
        };
    }

    private String successMessage(PaymentSecurityCommand command) {
        return switch (command.impactTarget()) {
            case ACCOUNT -> "Account amount blocked successfully.";
            case FX_DEAL -> "FX deal reserved successfully.";
            case FORWARD_CONTRACT -> "Forward contract amount reserved successfully.";
            case FINANCING_FOLDER -> "Financing folder amount reserved successfully.";
            case RECEIVED_FUNDS -> "Received funds reserved successfully.";
            case INTERBANK_COVER -> "Interbank cover reserved successfully.";
        };
    }
}