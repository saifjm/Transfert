package com.smi.mstr.transfer.application.payment;

import com.smi.mstr.transfer.domain.entity.TrPaymentModality;
import com.smi.mstr.transfer.domain.enums.PaymentResourceType;
import org.springframework.stereotype.Component;

@Component
public class PaymentResourceResolver {

    public PaymentResourceType resolveResourceType(TrPaymentModality modality) {
        return switch (modality.getModalityType()) {
            case TND_FX_PURCHASE_NORMAL,
                 TND_FX_PURCHASE_NEGOTIATED,
                 DIRECT_FOREIGN_CURRENCY_ACCOUNT_DEBIT,
                 CURRENCY_ARBITRAGE -> PaymentResourceType.ACCOUNT_BALANCE;

            case FORWARD_FX_CONTRACT -> PaymentResourceType.FORWARD_CONTRACT;
            case FUNDS_RECEIVED_LOCAL_BANK -> PaymentResourceType.RECEIVED_FUNDS;
            case INTERBANK_FX_COVER -> PaymentResourceType.INTERBANK_COVER;
            case IMPORT_FINANCING -> PaymentResourceType.FINANCING_LINE;
            case OTHER -> PaymentResourceType.ACCOUNT_BALANCE;
        };
    }

    public String resolveResourceRef(TrPaymentModality modality) {
        return switch (modality.getModalityType()) {
            case TND_FX_PURCHASE_NORMAL,
                 TND_FX_PURCHASE_NEGOTIATED,
                 DIRECT_FOREIGN_CURRENCY_ACCOUNT_DEBIT,
                 CURRENCY_ARBITRAGE -> modality.getDebitAccountRef();

            case FORWARD_FX_CONTRACT -> modality.getForwardContractRef();
            case FUNDS_RECEIVED_LOCAL_BANK -> modality.getReceivedFundsRef();
            case INTERBANK_FX_COVER -> modality.getInterbankCoverRef();
            case IMPORT_FINANCING -> modality.getFinancingRef();
            case OTHER -> modality.getDebitAccountRef();
        };
    }
}