package com.smi.mstr.transfer.application;

import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.domain.entity.TrParty;
import com.smi.mstr.transfer.domain.entity.TrPaymentModality;
import com.smi.mstr.transfer.domain.entity.TrSupportReglementaire;
import com.smi.mstr.transfer.domain.enums.BlockingStatus;
import com.smi.mstr.transfer.domain.enums.FxType;
import com.smi.mstr.transfer.domain.enums.PartyRole;
import com.smi.mstr.transfer.domain.enums.PartyType;
import com.smi.mstr.transfer.domain.enums.PaymentImpactStatus;
import com.smi.mstr.transfer.domain.enums.PaymentModalityStatus;
import com.smi.mstr.transfer.domain.enums.PaymentResourceType;
import com.smi.mstr.transfer.domain.enums.StatutImputationSupport;
import com.smi.mstr.transfer.domain.enums.StatutReservationSupport;
import com.smi.mstr.transfer.domain.enums.StatutSupportReglementaire;
import com.smi.mstr.transfer.domain.enums.StatutValidationSupport;
import com.smi.mstr.transfer.domain.enums.SwiftPriority;
import com.smi.mstr.transfer.dto.party.PartyCommandDto;
import com.smi.mstr.transfer.dto.payment.PaymentModalityCommandDto;
import com.smi.mstr.transfer.dto.regulatory.RegulatorySupportCommandDto;
import com.smi.mstr.transfer.dto.workflow.sections.InterbankDataSection;
import com.smi.mstr.transfer.dto.workflow.sections.PaymentModalitiesSection;
import com.smi.mstr.transfer.dto.workflow.sections.RegulatorySupportsSection;
import com.smi.mstr.transfer.dto.workflow.sections.TransferInstructionSection;
import com.smi.mstr.transfer.dto.workflow.sections.TransferPartiesSection;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class TransferAggregateAssembler {

    public void applyTransferInstruction(
            MvtTrOperation operation,
            TransferInstructionSection section
    ) {
        if (section == null) {
            return;
        }

        operation.setMntOrdre(section.orderAmount());
        operation.setCodeDeviseOrdre(upper(section.orderCurrency()));

        operation.setMntDevise(section.transferAmount());
        operation.setCodeDevise(upper(section.transferCurrency()));

        operation.setDateValeurTransfert(section.valueDate());

        operation.setCoursConversion(section.fxRate());
        operation.setContreValeurTnd(section.counterValueTnd());

        operation.setEndToEndId(clean(section.endToEndId()));

        operation.setSwiftPriority(SwiftPriority.from(section.swiftPriority()));
        operation.setServiceLevelCode(upper(section.serviceLevelCode()));
        operation.setCategoryPurposeCode(upper(section.categoryPurposeCode()));

        operation.setPurposeCode(clean(section.purposeCode()));
        operation.setPurposeProprietary(clean(section.purposeProprietary()));
        operation.setRemittanceUnstructured(clean(section.remittanceUnstructured()));

        operation.setChargeBearer(upper(section.chargeBearer()));
    }

    public void replaceParties(
            MvtTrOperation operation,
            TransferPartiesSection section
    ) {
        if (section == null || section.parties() == null) {
            return;
        }

        operation.clearParties();

        section.parties()
                .stream()
                .map(this::toParty)
                .forEach(operation::addParty);

        syncHeaderShortcutsFromParties(operation);
    }

    public void replacePaymentModalities(
            MvtTrOperation operation,
            PaymentModalitiesSection section
    ) {
        if (section == null || section.modalities() == null) {
            return;
        }

        operation.clearPaymentModalities();

        section.modalities()
                .stream()
                .map(this::toPaymentModality)
                .forEach(operation::addPaymentModality);
    }

    public void replaceRegulatorySupports(
            MvtTrOperation operation,
            RegulatorySupportsSection section
    ) {
        if (section == null || section.supports() == null) {
            return;
        }

        operation.clearSupportsReglementaires();

        section.supports()
                .stream()
                .map(this::toSupportReglementaire)
                .forEach(operation::addSupportReglementaire);
    }

    public void applyInterbankData(
            MvtTrOperation operation,
            InterbankDataSection section
    ) {
        if (section == null) {
            return;
        }

        operation.setRouteType(section.routeType());
        operation.setCoverRequired(normalizeYesNo(section.coverRequired()));
        operation.setSettlementMethod(upper(section.settlementMethod()));
        operation.setSettlementAccountRef(clean(section.settlementAccountRef()));
        operation.setInterbankSnapshotJson(section.interbankSnapshotJson());

        replaceInterbankParties(operation, section.interbankParties());

        syncHeaderShortcutsFromParties(operation);
    }

    private void replaceInterbankParties(
            MvtTrOperation operation,
            List<PartyCommandDto> interbankParties
    ) {
        if (interbankParties == null) {
            return;
        }

        operation.removePartiesByRole(
                PartyRole.INSTG_AGT,
                PartyRole.INSTD_AGT,
                PartyRole.DBTR_AGT,
                PartyRole.CDTR_AGT,
                PartyRole.INTRMY_AGT_1,
                PartyRole.INTRMY_AGT_2,
                PartyRole.INTRMY_AGT_3,
                PartyRole.COVER_AGT,
                PartyRole.NOSTRO_AGT,
                PartyRole.REIMBURSEMENT_AGT,
                PartyRole.SENDER_CORRESPONDENT,
                PartyRole.RECEIVER_CORRESPONDENT,
                PartyRole.SETTLEMENT_ACCT,
                PartyRole.NOSTRO_ACCT
        );

        interbankParties.stream()
                .map(this::toParty)
                .forEach(operation::addParty);
    }

    private TrParty toParty(PartyCommandDto dto) {
        if (dto == null) {
            return null;
        }

        return TrParty.builder()
                .sequenceNo(resolveSequenceNo(dto.sequenceNo()))
                .partyRole(dto.partyRole())
                .partyType(resolvePartyType(dto))

                .customerId(dto.customerId())
                .externalPartyRef(clean(dto.externalPartyRef()))
                .name(clean(dto.name()))
                .countryCode(upper(dto.countryCode()))
                .residencyStatus(dto.residencyStatus())

                .identificationType(upper(dto.identificationType()))
                .identificationValue(clean(dto.identificationValue()))
                .identificationIssuer(upper(dto.identificationIssuer()))
                .identificationScheme(upper(dto.identificationScheme()))
                .lei(upper(dto.lei()))

                .addressLine1(clean(dto.addressLine1()))
                .addressLine2(clean(dto.addressLine2()))
                .addressLine3(clean(dto.addressLine3()))
                .townName(clean(dto.townName()))
                .postCode(clean(dto.postCode()))
                .countrySubDivision(clean(dto.countrySubDivision()))

                .accountNumber(clean(dto.accountNumber()))
                .accountIban(upper(dto.accountIban()))
                .accountScheme(upper(dto.accountScheme()))
                .accountCurrency(upper(dto.accountCurrency()))
                .accountType(upper(dto.accountType()))
                .accountName(clean(dto.accountName()))

                .bic(upper(dto.bic()))
                .bankCode(clean(dto.bankCode()))
                .bankName(clean(dto.bankName()))
                .bankBranchCode(clean(dto.bankBranchCode()))
                .bankBranchName(clean(dto.bankBranchName()))
                .bankCountryCode(upper(dto.bankCountryCode()))
                .clearingSystemCode(upper(dto.clearingSystemCode()))
                .clearingMemberId(clean(dto.clearingMemberId()))

                .agentPosition(dto.agentPosition())
                .routingRole(upper(dto.routingRole()))

                .sourceSystem(upper(dto.sourceSystem()))
                .sourceReference(clean(dto.sourceReference()))
                .partySnapshotJson(dto.partySnapshotJson())

                .build();
    }

    private TrPaymentModality toPaymentModality(PaymentModalityCommandDto dto) {
        if (dto == null) {
            return null;
        }

        TrPaymentModality modality = TrPaymentModality.builder()
                .sequenceNo(resolveSequenceNo(dto.sequenceNo()))
                .modalityType(dto.modalityType())

                .coveragePercentage(dto.coveragePercentage())
                .coveredTransferAmount(dto.coveredTransferAmount())
                .coveredTransferCurrency(upper(dto.coveredTransferCurrency()))

                .debitAccountNumber(clean(dto.debitAccountNumber()))
                .debitAccountCurrency(upper(dto.debitAccountCurrency()))
                .debitAmount(dto.debitAmount())

                .fxType(resolveFxType(dto))
                .fxRate(dto.fxRate())
                .fxRateDate(dto.fxRate() == null ? null : LocalDateTime.now())
                .fxReference(clean(dto.fxReference()))

                .resourceType(resolveResourceType(dto))
                .resourceReference(resolveResourceReference(dto))

                .fxRequired(resolveFxRequired(dto))
                .blockingRequired(resolveBlockingRequired(dto))
                .blockingStatus(BlockingStatus.TO_BLOCK)
                .impactStatus(PaymentImpactStatus.PENDING)
                .modalityStatus(PaymentModalityStatus.DRAFT)

                .build();

        if (!modality.requiresBlocking()) {
            modality.setBlockingStatus(BlockingStatus.NOT_REQUIRED);
        }

        return modality;
    }

    private TrSupportReglementaire toSupportReglementaire(
            RegulatorySupportCommandDto dto
    ) {
        if (dto == null) {
            return null;
        }

        TrSupportReglementaire support = TrSupportReglementaire.builder()
                .sequenceNo(resolveSequenceNo(dto.sequenceNo()))
                .typeSupport(dto.typeSupport())
                .codeSupportBct(dto.codeSupportBct())
                .numSupport(clean(dto.numSupport()))
                .dateSupport(dto.dateSupport())
                .autoriteEmettrice(clean(dto.autoriteEmettrice()))

                .numIdentification(clean(dto.numIdentification()))
                .dateIdentification(dto.dateIdentification())
                .codeRd(upper(dto.codeRd()))
                .modeReglement(dto.modeReglement())
                .numMessageSwift(clean(dto.numMessageSwift()))
                .codeBanque(clean(dto.codeBanque()))

                .deviseSupport(upper(dto.deviseSupport()))
                .montantAutorise(dto.montantAutorise())
                .montantUtiliseAvant(dto.montantUtiliseAvant())
                .montantReserve(dto.montantReserve())
                .montantUtiliseCourant(dto.montantUtiliseCourant())
                .reliquatAvant(dto.reliquatAvant())
                .reliquatApres(dto.reliquatApres())
                .montantTnd(dto.montantTnd())
                .coursConversion(dto.coursConversion())

                .statutSupport(StatutSupportReglementaire.DRAFT)
                .statutValidation(resolveInitialValidationStatus(dto))
                .statutReservation(resolveInitialReservationStatus(dto))
                .statutImputation(StatutImputationSupport.PENDING)

                .messageValidation(clean(dto.messageValidation()))
                .sourceSystem(upper(dto.sourceSystem()))
                .sourceReference(clean(dto.sourceReference()))
                .snapshotSupportJson(dto.snapshotSupportJson())

                .build();

        return support;
    }

    private void syncHeaderShortcutsFromParties(MvtTrOperation operation) {
        TrParty ultimateDebtor = findParty(operation, PartyRole.ULTMT_DBTR);
        TrParty debtor = findParty(operation, PartyRole.DBTR);
        TrParty creditor = findParty(operation, PartyRole.CDTR);
        TrParty ultimateCreditor = findParty(operation, PartyRole.ULTMT_CDTR);
        TrParty creditorAgent = findParty(operation, PartyRole.CDTR_AGT);
        TrParty initgPty = findParty(operation, PartyRole.INITG_PTY);
        TrParty instgAgt = findParty(operation, PartyRole.INSTG_AGT);
        TrParty instdAgt = findParty(operation, PartyRole.INSTD_AGT);
        TrParty coverAgt = findParty(operation, PartyRole.COVER_AGT);

        TrParty creditorAccount = findParty(operation, PartyRole.CDTR_ACCT);
        TrParty chargesAccount = findParty(operation, PartyRole.CHARGES_ACCT);

        operation.setUltimateDebtorId(resolvePartyShortcutId(ultimateDebtor));
        operation.setDebtorId(resolvePartyShortcutId(debtor));
        operation.setCreditorId(resolvePartyShortcutId(creditor));
        operation.setUltimateCreditorId(resolvePartyShortcutId(ultimateCreditor));
        operation.setCreditorAgentId(resolvePartyShortcutId(creditorAgent));
        operation.setInitgPtyId(resolvePartyShortcutId(initgPty));
        operation.setInstgAgtId(resolvePartyShortcutId(instgAgt));
        operation.setInstdAgtId(resolvePartyShortcutId(instdAgt));
        operation.setCoverAgtId(resolvePartyShortcutId(coverAgt));

        operation.setNoCompteCreditor(resolveAccountReference(creditorAccount));
        operation.setNoCompteCommission(resolveAccountReference(chargesAccount));
    }

    private TrParty findParty(MvtTrOperation operation, PartyRole role) {
        return operation.findParty(role).orElse(null);
    }

    private Long resolvePartyShortcutId(TrParty party) {
        if (party == null) {
            return null;
        }

        if (party.getCustomerId() != null) {
            return party.getCustomerId();
        }

        return tryParseLong(party.getExternalPartyRef());
    }

    private String resolveAccountReference(TrParty party) {
        if (party == null) {
            return null;
        }

        if (notBlank(party.getAccountIban())) {
            return party.getAccountIban();
        }

        if (notBlank(party.getAccountNumber())) {
            return party.getAccountNumber();
        }

        return party.getExternalPartyRef();
    }

    private PartyType resolvePartyType(PartyCommandDto dto) {
        if (dto.partyType() != null) {
            return dto.partyType();
        }

        if (dto.partyRole() == null) {
            return PartyType.OTHER;
        }

        if (dto.partyRole().isAccount()) {
            return PartyType.ACCOUNT;
        }

        if (dto.partyRole().isFinancialAgent()) {
            return PartyType.BANK;
        }

        if (dto.partyRole().isInternal()) {
            return PartyType.INTERNAL;
        }

        return PartyType.ORG;
    }

    private FxType resolveFxType(PaymentModalityCommandDto dto) {
        if (dto.fxType() != null) {
            return dto.fxType();
        }

        if (dto.modalityType() != null && dto.modalityType().fxRequiredByDefault()) {
            return FxType.NORMAL;
        }

        return FxType.NOT_REQUIRED;
    }

    private PaymentResourceType resolveResourceType(PaymentModalityCommandDto dto) {
        if (dto.modalityType() == null) {
            return PaymentResourceType.OTHER;
        }

        return dto.modalityType().defaultResourceType();
    }

    private String resolveResourceReference(PaymentModalityCommandDto dto) {
        if (notBlank(dto.resourceReference())) {
            return clean(dto.resourceReference());
        }

        if (notBlank(dto.debitAccountNumber())) {
            return clean(dto.debitAccountNumber());
        }

        if (notBlank(dto.fxReference())) {
            return clean(dto.fxReference());
        }

        return null;
    }

    private String resolveFxRequired(PaymentModalityCommandDto dto) {
        if (dto.modalityType() != null && dto.modalityType().fxRequiredByDefault()) {
            return "Y";
        }

        if (dto.fxType() != null && dto.fxType() != FxType.NOT_REQUIRED) {
            return "Y";
        }

        if (dto.fxRate() != null || notBlank(dto.fxReference())) {
            return "Y";
        }

        return "N";
    }

    private String resolveBlockingRequired(PaymentModalityCommandDto dto) {
        if (dto.modalityType() == null) {
            return "Y";
        }

        return dto.modalityType().blockingRequiredByDefault() ? "Y" : "N";
    }

    private StatutValidationSupport resolveInitialValidationStatus(
            RegulatorySupportCommandDto dto
    ) {
        if (dto.typeSupport() == null) {
            return StatutValidationSupport.PENDING;
        }

        return dto.typeSupport().validationRequired()
                ? StatutValidationSupport.PENDING
                : StatutValidationSupport.NOT_REQUIRED;
    }

    private StatutReservationSupport resolveInitialReservationStatus(
            RegulatorySupportCommandDto dto
    ) {
        if (dto.typeSupport() == null) {
            return StatutReservationSupport.NOT_RESERVED;
        }

        return dto.typeSupport().reservationRequired()
                ? StatutReservationSupport.NOT_RESERVED
                : StatutReservationSupport.NOT_REQUIRED;
    }

    private Integer resolveSequenceNo(Integer sequenceNo) {
        return sequenceNo == null ? 1 : sequenceNo;
    }

    private Long tryParseLong(String value) {
        if (isBlank(value)) {
            return null;
        }

        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String normalizeYesNo(String value) {
        if (isBlank(value)) {
            return "N";
        }

        String normalized = value.trim().toUpperCase();

        if ("O".equals(normalized)
                || "Y".equals(normalized)
                || "YES".equals(normalized)
                || "TRUE".equals(normalized)) {
            return "Y";
        }

        return "N";
    }

    private String upper(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }

    private String clean(String value) {
        return value == null ? null : value.trim();
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}