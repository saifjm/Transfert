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
            return unavailable(
                    command,
                    PaymentResourceAvailabilityStatus.ERROR,
                    "Payment resource reference is missing."
            );
        }

        return switch (command.impactTarget()) {
            case ACCOUNT -> checkAccount(command);
            case FINANCING_FOLDER -> checkFinancingFolder(command);
            case FX_DEAL -> checkFxDeal(command);
            case FORWARD_CONTRACT -> checkForwardContract(command);
            case RECEIVED_FUNDS -> checkReceivedFunds(command);
            case INTERBANK_COVER -> checkInterbankCover(command);
        };
    }

    private PaymentResourceAvailabilityItemDto checkAccount(PaymentResourceCommand command) {
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
                    "Account balance is insufficient."
            );
        }

        if (command.resourceRef().endsWith("BLOCKED")) {
            return unavailable(
                    command,
                    PaymentResourceAvailabilityStatus.UNAVAILABLE,
                    "Account is currently unavailable or blocked."
            );
        }

        return available(
                command,
                new BigDecimal("999999999.000"),
                "Account balance is available."
        );
    }

    private PaymentResourceAvailabilityItemDto checkFinancingFolder(PaymentResourceCommand command) {
        if (command.resourceRef().endsWith("EXHAUSTED")) {
            return new PaymentResourceAvailabilityItemDto(
                    command.modalityId(),
                    command.resourceType(),
                    command.resourceRef(),
                    command.amountToCheck(),
                    command.currencyToCheck(),
                    BigDecimal.ZERO,
                    command.currencyToCheck(),
                    PaymentResourceAvailabilityStatus.INSUFFICIENT,
                    "Financing folder available amount is insufficient."
            );
        }

        if (command.resourceRef().endsWith("CLOSED")) {
            return unavailable(
                    command,
                    PaymentResourceAvailabilityStatus.UNAVAILABLE,
                    "Financing folder is closed."
            );
        }

        return available(
                command,
                new BigDecimal("500000.000"),
                "Financing folder is available."
        );
    }

    private PaymentResourceAvailabilityItemDto checkFxDeal(PaymentResourceCommand command) {
        if (command.resourceRef().endsWith("USED")) {
            return unavailable(
                    command,
                    PaymentResourceAvailabilityStatus.UNAVAILABLE,
                    "FX deal has already been fully used."
            );
        }

        if (command.resourceRef().endsWith("LOW")) {
            return new PaymentResourceAvailabilityItemDto(
                    command.modalityId(),
                    command.resourceType(),
                    command.resourceRef(),
                    command.amountToCheck(),
                    command.currencyToCheck(),
                    new BigDecimal("1000.000"),
                    command.currencyToCheck(),
                    PaymentResourceAvailabilityStatus.INSUFFICIENT,
                    "FX deal remaining amount is insufficient."
            );
        }

        return available(
                command,
                new BigDecimal("1000000.000"),
                "FX deal is available."
        );
    }

    private PaymentResourceAvailabilityItemDto checkForwardContract(PaymentResourceCommand command) {
        if (command.resourceRef().endsWith("EXPIRED")) {
            return unavailable(
                    command,
                    PaymentResourceAvailabilityStatus.UNAVAILABLE,
                    "Forward contract is expired."
            );
        }

        if (command.resourceRef().endsWith("USED")) {
            return unavailable(
                    command,
                    PaymentResourceAvailabilityStatus.UNAVAILABLE,
                    "Forward contract has already been fully used."
            );
        }

        return available(
                command,
                new BigDecimal("750000.000"),
                "Forward contract is available."
        );
    }

    private PaymentResourceAvailabilityItemDto checkReceivedFunds(PaymentResourceCommand command) {
        if (command.resourceRef().endsWith("UNMATCHED")) {
            return unavailable(
                    command,
                    PaymentResourceAvailabilityStatus.UNAVAILABLE,
                    "Received funds are not matched with the transfer operation."
            );
        }

        if (command.resourceRef().endsWith("LOW")) {
            return new PaymentResourceAvailabilityItemDto(
                    command.modalityId(),
                    command.resourceType(),
                    command.resourceRef(),
                    command.amountToCheck(),
                    command.currencyToCheck(),
                    new BigDecimal("500.000"),
                    command.currencyToCheck(),
                    PaymentResourceAvailabilityStatus.INSUFFICIENT,
                    "Received funds amount is insufficient."
            );
        }

        return available(
                command,
                new BigDecimal("300000.000"),
                "Received funds are available."
        );
    }

    private PaymentResourceAvailabilityItemDto checkInterbankCover(PaymentResourceCommand command) {
        if (command.resourceRef().endsWith("REJECTED")) {
            return unavailable(
                    command,
                    PaymentResourceAvailabilityStatus.UNAVAILABLE,
                    "Interbank cover request was rejected."
            );
        }

        if (command.resourceRef().endsWith("PENDING")) {
            return unavailable(
                    command,
                    PaymentResourceAvailabilityStatus.UNAVAILABLE,
                    "Interbank cover is still pending confirmation."
            );
        }

        return available(
                command,
                new BigDecimal("2000000.000"),
                "Interbank cover is available."
        );
    }

    private PaymentResourceAvailabilityItemDto available(
            PaymentResourceCommand command,
            BigDecimal availableAmount,
            String message
    ) {
        return new PaymentResourceAvailabilityItemDto(
                command.modalityId(),
                command.resourceType(),
                command.resourceRef(),
                command.amountToCheck(),
                command.currencyToCheck(),
                availableAmount,
                command.currencyToCheck(),
                PaymentResourceAvailabilityStatus.AVAILABLE,
                message
        );
    }

    private PaymentResourceAvailabilityItemDto unavailable(
            PaymentResourceCommand command,
            PaymentResourceAvailabilityStatus status,
            String message
    ) {
        return new PaymentResourceAvailabilityItemDto(
                command.modalityId(),
                command.resourceType(),
                command.resourceRef(),
                command.amountToCheck(),
                command.currencyToCheck(),
                BigDecimal.ZERO,
                command.currencyToCheck(),
                status,
                message
        );
    }
}