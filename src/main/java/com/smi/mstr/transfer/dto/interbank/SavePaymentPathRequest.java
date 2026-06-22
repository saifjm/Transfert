package com.smi.mstr.transfer.dto.interbank;

import java.util.List;

public record SavePaymentPathRequest(
        String updatedBy,
        String comment,
        List<InterbankAgentDto> agents
) {
}