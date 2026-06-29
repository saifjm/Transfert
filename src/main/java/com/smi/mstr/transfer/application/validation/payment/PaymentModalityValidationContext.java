package com.smi.mstr.transfer.application.validation.payment;

import com.smi.mstr.transfer.application.context.WorkflowCommandContext;
import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.domain.entity.TrPaymentModality;

import java.math.BigDecimal;
import java.util.List;

public record PaymentModalityValidationContext(
        MvtTrOperation operation,
        List<TrPaymentModality> modalities,
        WorkflowCommandContext workflowContext
) {

    public BigDecimal transferAmount() {
        return operation == null ? null : operation.getMntDevise();
    }

    public String transferCurrency() {
        return operation == null ? null : operation.getCodeDevise();
    }

    public BigDecimal counterValueTnd() {
        return operation == null ? null : operation.getContreValeurTnd();
    }

    public String branchCode() {
        if (workflowContext != null && workflowContext.branchCode() != null) {
            return workflowContext.branchCode();
        }

        return operation == null ? null : operation.getCodeAgence();
    }
}
