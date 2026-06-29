package com.smi.mstr.transfer.application.validation.payment;

import com.smi.mstr.transfer.domain.entity.TrPaymentModality;

import java.util.ArrayList;
import java.util.List;

public class PaymentModalityValidationCollector {

    private final List<PaymentModalityValidationIssue> issues = new ArrayList<>();

    public void error(
            TrPaymentModality modality,
            String code,
            String field,
            String message
    ) {
        add(
                PaymentModalityValidationSeverity.ERROR,
                modality,
                code,
                field,
                message
        );
    }

    public void warning(
            TrPaymentModality modality,
            String code,
            String field,
            String message
    ) {
        add(
                PaymentModalityValidationSeverity.WARNING,
                modality,
                code,
                field,
                message
        );
    }

    public void globalError(
            String code,
            String field,
            String message
    ) {
        issues.add(new PaymentModalityValidationIssue(
                PaymentModalityValidationSeverity.ERROR,
                code,
                field,
                message,
                null,
                null
        ));
    }

    public void globalWarning(
            String code,
            String field,
            String message
    ) {
        issues.add(new PaymentModalityValidationIssue(
                PaymentModalityValidationSeverity.WARNING,
                code,
                field,
                message,
                null,
                null
        ));
    }

    public PaymentModalityValidationReport toReport() {
        return new PaymentModalityValidationReport(issues);
    }

    private void add(
            PaymentModalityValidationSeverity severity,
            TrPaymentModality modality,
            String code,
            String field,
            String message
    ) {
        issues.add(new PaymentModalityValidationIssue(
                severity,
                code,
                field,
                message,
                modality == null ? null : modality.getSequenceNo(),
                modality == null ? null : modality.getModalityType()
        ));
    }
}