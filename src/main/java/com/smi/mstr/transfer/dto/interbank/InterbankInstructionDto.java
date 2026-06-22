package com.smi.mstr.transfer.dto.interbank;

import com.smi.mstr.transfer.domain.enums.FinancialAgentRole;
import com.smi.mstr.transfer.domain.enums.InterbankInstructionTargetFormat;
import com.smi.mstr.transfer.domain.enums.InterbankInstructionType;

public record InterbankInstructionDto(
        InterbankInstructionType instructionType,
        InterbankInstructionTargetFormat targetFormat,
        FinancialAgentRole targetAgentRole,
        String instructionCode,
        String instructionText
) {
}