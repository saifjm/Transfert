package com.smi.mstr.transfer.application.validation.payment;

import java.util.List;

public record PaymentModalityValidationReport(
        List<PaymentModalityValidationIssue> issues
) {

    public PaymentModalityValidationReport {
        issues = issues == null ? List.of() : List.copyOf(issues);
    }

    public boolean valid() {
        return errors().isEmpty();
    }

    public boolean hasErrors() {
        return !errors().isEmpty();
    }

    public boolean hasWarnings() {
        return !warnings().isEmpty();
    }

    public List<PaymentModalityValidationIssue> errors() {
        return issues.stream()
                .filter(issue -> issue.severity() == PaymentModalityValidationSeverity.ERROR)
                .toList();
    }

    public List<PaymentModalityValidationIssue> warnings() {
        return issues.stream()
                .filter(issue -> issue.severity() == PaymentModalityValidationSeverity.WARNING)
                .toList();
    }
}
