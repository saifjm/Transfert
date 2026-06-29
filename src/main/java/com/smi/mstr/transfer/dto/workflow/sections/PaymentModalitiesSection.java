package com.smi.mstr.transfer.dto.workflow.sections;

import com.smi.mstr.transfer.dto.payment.PaymentModalityCommandDto;

import java.util.List;

public record PaymentModalitiesSection(
        List<PaymentModalityCommandDto> modalities
) {
}