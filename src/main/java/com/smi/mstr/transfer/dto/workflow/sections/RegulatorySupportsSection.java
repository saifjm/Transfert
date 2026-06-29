package com.smi.mstr.transfer.dto.workflow.sections;

import com.smi.mstr.transfer.dto.regulatory.RegulatorySupportCommandDto;

import java.util.List;

public record RegulatorySupportsSection(
        List<RegulatorySupportCommandDto> supports
) {
}