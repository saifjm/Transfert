package com.smi.mstr.transfer.application.funds;

import com.smi.mstr.transfer.domain.enums.FundsBlockingStatus;
import com.smi.mstr.transfer.dto.payment.FundsBlockingItemDto;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@ConditionalOnProperty(
        name = "mstr.funds.blocking.client",
        havingValue = "mock",
        matchIfMissing = true
)
public class MockFundsBlockingClient implements FundsBlockingClient {

    @Override
    public FundsBlockingItemDto blockFunds(
            Long modalityId,
            String debitAccountRef,
            BigDecimal amount,
            String currency
    ) {
        if (debitAccountRef == null || debitAccountRef.isBlank()) {
            return new FundsBlockingItemDto(
                    modalityId,
                    debitAccountRef,
                    true,
                    amount,
                    currency,
                    null,
                    null,
                    null,
                    FundsBlockingStatus.FAILED,
                    "Debit account reference is missing."
            );
        }

        if (debitAccountRef.endsWith("BLKERR")) {
            return new FundsBlockingItemDto(
                    modalityId,
                    debitAccountRef,
                    true,
                    amount,
                    currency,
                    null,
                    null,
                    null,
                    FundsBlockingStatus.FAILED,
                    "Mock blocking failure."
            );
        }

        String reference = "BLK-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();

        return new FundsBlockingItemDto(
                modalityId,
                debitAccountRef,
                true,
                amount,
                currency,
                amount,
                currency,
                reference,
                FundsBlockingStatus.BLOCKED,
                "Funds blocked successfully."
        );
    }
}