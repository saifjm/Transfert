package com.smi.mstr.transfer.application.funds;

import com.smi.mstr.transfer.domain.enums.FundsCheckStatus;
import com.smi.mstr.transfer.dto.payment.FundsAvailabilityItemDto;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConditionalOnProperty(
        name = "mstr.funds.client",
        havingValue = "mock",
        matchIfMissing = true
)
public class MockFundsAvailabilityClient implements FundsAvailabilityClient {

    @Override
    public FundsAvailabilityItemDto checkAvailability(
            Long modalityId,
            String debitAccountRef,
            BigDecimal requestedAmount,
            String requestedCurrency
    ) {
        if (debitAccountRef == null || debitAccountRef.isBlank()) {
            return new FundsAvailabilityItemDto(
                    modalityId,
                    debitAccountRef,
                    requestedAmount,
                    requestedCurrency,
                    BigDecimal.ZERO,
                    FundsCheckStatus.ERROR,
                    "Debit account reference is missing."
            );
        }

        /*
         * Simple DEV rule:
         * - accounts ending with LOW simulate insufficient balance
         * - all other accounts simulate sufficient funds
         */
        BigDecimal availableAmount = debitAccountRef.endsWith("LOW")
                ? new BigDecimal("100.000")
                : new BigDecimal("9999999.000");

        FundsCheckStatus status = availableAmount.compareTo(requestedAmount) >= 0
                ? FundsCheckStatus.SUFFICIENT
                : FundsCheckStatus.INSUFFICIENT;

        String message = status == FundsCheckStatus.SUFFICIENT
                ? "Funds are available."
                : "Insufficient funds on debit account.";

        return new FundsAvailabilityItemDto(
                modalityId,
                debitAccountRef,
                requestedAmount,
                requestedCurrency,
                availableAmount,
                status,
                message
        );
    }
}