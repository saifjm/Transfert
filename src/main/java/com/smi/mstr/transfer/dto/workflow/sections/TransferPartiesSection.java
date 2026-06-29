package com.smi.mstr.transfer.dto.workflow.sections;

import com.smi.mstr.transfer.dto.party.PartyCommandDto;

import java.util.List;

public record TransferPartiesSection(
        List<PartyCommandDto> parties
) {
}