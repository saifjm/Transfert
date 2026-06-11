package com.smi.mstr.transfer.application.payment;

import com.smi.mstr.transfer.domain.enums.PaymentModalityType;
import com.smi.mstr.transfer.dto.payment.PaymentModalityDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class PaymentModalityValidator {

    public void validate(List<PaymentModalityDto> modalities) {
        if (modalities == null || modalities.isEmpty()) {
            throw new IllegalArgumentException("At least one payment modality is required.");
        }

        for (PaymentModalityDto modality : modalities) {
            validateOne(modality);
        }
    }

    private void validateOne(PaymentModalityDto modality) {
        if (modality.modalityType() == null) {
            throw new IllegalArgumentException("Payment modality type is required.");
        }

        if (modality.targetAmount() == null
                || modality.targetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Target amount must be strictly positive.");
        }

        if (blank(modality.targetCurrency())) {
            throw new IllegalArgumentException("Target currency is required.");
        }

        PaymentModalityType type = modality.modalityType();

        switch (type) {
            case TND_FX_PURCHASE_NORMAL,
                 TND_FX_PURCHASE_NEGOTIATED -> {
                require(modality.debitAccountRef(), "Debit account reference is required for TND FX purchase.");
                require(modality.debitAccountCurrency(), "Debit account currency is required for TND FX purchase.");
                require(modality.fxRate(), "FX rate is required for TND FX purchase.");
            }

            case FORWARD_FX_CONTRACT ->
                    require(modality.forwardContractRef(), "Forward contract reference is required.");

            case DIRECT_FOREIGN_CURRENCY_ACCOUNT_DEBIT -> {
                require(modality.debitAccountRef(), "Debit account reference is required.");
                require(modality.debitAccountCurrency(), "Debit account currency is required.");
            }

            case CURRENCY_ARBITRAGE -> {
                require(modality.sourceAmount(), "Source amount is required for currency arbitrage.");
                require(modality.sourceCurrency(), "Source currency is required for currency arbitrage.");
                require(modality.targetCurrency(), "Target currency is required for currency arbitrage.");
                require(modality.fxRate(), "Arbitrage FX rate is required.");
            }

            case FUNDS_RECEIVED_LOCAL_BANK ->
                    require(modality.receivedFundsRef(), "Received funds reference is required.");

            case INTERBANK_FX_COVER ->
                    require(modality.interbankCoverRef(), "Interbank cover reference is required.");

            case IMPORT_FINANCING ->
                    require(modality.financingRef(), "Financing reference is required.");

            case OTHER -> {
                // No specific mandatory resource at this stage.
            }
        }
    }

    private void require(String value, String message) {
        if (blank(value)) {
            throw new IllegalArgumentException(message);
        }
    }

    private void require(BigDecimal value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}