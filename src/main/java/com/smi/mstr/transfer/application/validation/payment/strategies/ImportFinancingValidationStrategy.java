package com.smi.mstr.transfer.application.validation.payment.strategies;

import com.smi.mstr.transfer.application.validation.payment.PaymentModalityValidationCollector;
import com.smi.mstr.transfer.application.validation.payment.PaymentModalityValidationContext;
import com.smi.mstr.transfer.application.validation.payment.PaymentModalityValidationStrategy;
import com.smi.mstr.transfer.domain.entity.TrPaymentModality;
import com.smi.mstr.transfer.domain.enums.PaymentModalityType;
import com.smi.mstr.transfer.domain.enums.PaymentResourceType;
import com.smi.mstr.transfer.domain.enums.TransferType;
import org.springframework.stereotype.Component;

@Component
public class ImportFinancingValidationStrategy
        extends AbstractPaymentModalityValidationStrategy
        implements PaymentModalityValidationStrategy {

    @Override
    public boolean supports(PaymentModalityType modalityType) {
        return modalityType == PaymentModalityType.IMPORT_FINANCING;
    }

    @Override
    public void validate(
            TrPaymentModality modality,
            PaymentModalityValidationContext context,
            PaymentModalityValidationCollector collector
    ) {
        requireResourceType(modality, PaymentResourceType.FINANCING_FILE, collector);
        requireResourceReference(
                modality,
                collector,
                "Import financing modality requires financing file reference."
        );

        if (context.operation() != null
                && context.operation().getTypeTransfert() != TransferType.C) {
            collector.error(
                    modality,
                    "PAYMOD_IMPORT_FINANCING_ONLY_COMMERCIAL",
                    "modalityType",
                    "Import financing is allowed only for commercial transfers."
            );
        }
    }
}
