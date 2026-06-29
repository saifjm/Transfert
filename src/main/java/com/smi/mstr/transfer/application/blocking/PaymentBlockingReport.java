package com.smi.mstr.transfer.application.blocking;

import java.util.List;

public record PaymentBlockingReport(
        List<PaymentBlockingIssue> issues
) {

    public PaymentBlockingReport {
        issues = issues == null ? List.of() : List.copyOf(issues);
    }

    public boolean success() {
        return issues.isEmpty();
    }

    public boolean hasErrors() {
        return !issues.isEmpty();
    }
}
