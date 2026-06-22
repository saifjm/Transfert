package com.smi.mstr.transfer.application.ref;

import com.smi.mstr.transfer.domain.enums.FinancialAgentRole;

public record RefInterbankAgentDto(
        FinancialAgentRole agentRole,
        String bicfi,
        String lei,
        String clearingSystemCode,
        String clearingMemberId,
        String agentName,
        String branchId,
        String branchName,
        String country,
        String addressLine1,
        String addressLine2,
        String townName
) {
}