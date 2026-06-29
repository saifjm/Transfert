package com.smi.mstr.transfer.application.blocking;

public class PaymentBlockingException extends RuntimeException {

    private final PaymentBlockingReport report;

    public PaymentBlockingException(PaymentBlockingReport report) {
        super(buildMessage(report));
        this.report = report;
    }

    public PaymentBlockingReport getReport() {
        return report;
    }

    private static String buildMessage(PaymentBlockingReport report) {
        if (report == null || report.issues().isEmpty()) {
            return "Payment blocking failed.";
        }

        return report.issues()
                .stream()
                .map(issue -> issue.code() + " - " + issue.message())
                .findFirst()
                .orElse("Payment blocking failed.");
    }
}
