package com.smi.mstr.transfer.dto.normalized;

public record FinancialAgentDto(
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
) {}
