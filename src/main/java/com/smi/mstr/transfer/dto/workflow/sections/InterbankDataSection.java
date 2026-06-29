package com.smi.mstr.transfer.dto.workflow.sections;

import com.smi.mstr.transfer.domain.enums.InterbankRouteType;
import com.smi.mstr.transfer.dto.party.PartyCommandDto;

import java.util.List;

public record InterbankDataSection(
        InterbankRouteType routeType,
        String coverRequired,
        String settlementMethod,
        String settlementAccountRef,
        String interbankSnapshotJson,

        List<PartyCommandDto> interbankParties
) {
}