package com.smi.mstr.transfer.dto.interbank;

import java.util.List;

public record SaveInterbankInstructionsRequest(
        String updatedBy,
        String comment,
        List<InterbankInstructionDto> instructions
) {
}