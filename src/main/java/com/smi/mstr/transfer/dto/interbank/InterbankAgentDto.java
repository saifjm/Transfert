package com.smi.mstr.transfer.dto.interbank;

import com.smi.mstr.transfer.domain.enums.FinancialAgentRole;

public record InterbankAgentDto(
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