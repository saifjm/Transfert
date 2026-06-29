package com.smi.mstr.transfer.application.validation.payment;

public class PaymentModalityValidationException extends RuntimeException {

    private final PaymentModalityValidationReport report;

    public PaymentModalityValidationException(PaymentModalityValidationReport report) {
        super(buildMessage(report));
        this.report = report;
    }

    public PaymentModalityValidationReport getReport() {
        return report;
    }

    private static String buildMessage(PaymentModalityValidationReport report) {
        if (report == null || report.errors().isEmpty()) {
            return "Payment modalities validation failed.";
        }

        return report.errors()
                .stream()
                .map(issue -> issue.code() + " - " + issue.message())
                .findFirst()
                .orElse("Payment modalities validation failed.");
    }
}