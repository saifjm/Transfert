package com.smi.mstr.transfer.dto.workflow.sections;

public record ValidationDecisionSection(
        String decision,
        String comment,
        Boolean generateFicheInformation
) {
}