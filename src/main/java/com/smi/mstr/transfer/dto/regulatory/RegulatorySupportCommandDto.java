package com.smi.mstr.transfer.dto.regulatory;

import com.smi.mstr.transfer.domain.enums.TypeSupportReglementaire;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegulatorySupportCommandDto(
        Integer sequenceNo,
        TypeSupportReglementaire typeSupport,
        Integer codeSupportBct,
        String numSupport,
        LocalDate dateSupport,
        String autoriteEmettrice,

        String numIdentification,
        LocalDate dateIdentification,

        String codeRd,
        Integer modeReglement,
        String numMessageSwift,
        String codeBanque,

        String deviseSupport,
        BigDecimal montantAutorise,
        BigDecimal montantUtiliseAvant,
        BigDecimal montantReserve,
        BigDecimal montantUtiliseCourant,
        BigDecimal reliquatAvant,
        BigDecimal reliquatApres,
        BigDecimal montantTnd,
        BigDecimal coursConversion,

        String messageValidation,
        String sourceSystem,
        String sourceReference,
        String snapshotSupportJson
) {
}