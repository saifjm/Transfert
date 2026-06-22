package com.smi.mstr.transfer.application;

import com.smi.mstr.transfer.application.context.TransferCreationContext;
import com.smi.mstr.transfer.application.mapper.TransferOperationResponseMapper;
import com.smi.mstr.transfer.domain.entity.*;
import com.smi.mstr.transfer.domain.enums.*;
import com.smi.mstr.transfer.domain.repository.MvtTrOperationRepository;
import com.smi.mstr.transfer.domain.repository.TrOperationValidationErrorRepository;
import com.smi.mstr.transfer.dto.*;
import com.smi.mstr.transfer.dto.normalized.AccountDto;
import com.smi.mstr.transfer.dto.normalized.PartyDto;
import com.smi.mstr.transfer.dto.normalized.PartyIdentificationDto;
import com.smi.mstr.transfer.dto.normalized.PostalAddressDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.smi.mstr.transfer.dto.normalized.FinancialAgentDto;

@Service
@RequiredArgsConstructor
public class TransferOperationService {

    private static final String AGENT_SAISIE_ROLE = "AGENT_SAISIE";

    private final MvtTrOperationRepository operationRepository;
    private final TransferReferenceService referenceService;
    private final TransferOperationResponseMapper responseMapper;
    private final TransferOrderValidationService validationService;
    private final TrOperationValidationErrorRepository validationErrorRepository;
    private final TransferOperationEventService eventService;
    private final TransferOperationLookupService operationLookupService;

    /**
     * Création d'un ordre manuel.
     *
     * Nouveau modèle :
     * - REF_OPERATION est généré par séquence BD.
     * - REF_ORDRE peut être générée après sauvegarde, car elle peut dépendre de REF_OPERATION.
     * - NUM_DOSSIER peut venir de la requête ou être généré.
     */
    @Transactional
    public TransferOperationResponse createManualOrder(
            CreateTransferOrderRequest request,
            TransferCreationContext context
    ) {
        MvtTrOperation operation = new MvtTrOperation();

        operation.setDateOperation(LocalDate.now());
        operation.setDateDossier(LocalDate.now());
        operation.setCreatedAt(LocalDateTime.now());

        operation.setCorrelationId(resolveCorrelationId(context));
        operation.setCodeAgence(context.branchCode());
        operation.setSourceChannel(context.sourceChannel());
        operation.setSourceModule(context.sourceModule());
        operation.setSourceReference(context.sourceReference());

        operation.setWorkflowInstanceId(context.workflowInstanceId());
        operation.setWorkflowTaskId(context.workflowTaskId());
        operation.setWorkflowContextJson(context.workflowContextJson());

        operation.setStatus(TransferOperationStatus.X);
        operation.setTypeTransfert(request.transferType());

        operation.setCodeOperation(resolveCodeOperation(request, context));

        applyClientData(operation, request);

        operation = operationRepository.saveAndFlush(operation);

        operation.setNumDossier(referenceService.generateNumDossier(operation));
        operation.setRefOrdre(referenceService.generateRefOrdre(operation));

        if (isBlank(operation.getEndToEndId())) {
            operation.setEndToEndId(referenceService.generateEndToEndId(operation));
        }

        if (isBlank(operation.getUetr())) {
            operation.setUetr(referenceService.generateUetr());
        }

        MvtTrOperation saved = operationRepository.save(operation);

        eventService.registerEvent(
                saved,
                OperationEventType.OPERATION_CREATED,
                null,
                saved.getStatus(),
                context.connectedUserId(),
                context.connectedUserRole(),
                "Manual transfer order created",
                null
        );

        return responseMapper.toResponse(saved);
    }

    /**
     * Sauvegarde brouillon.
     *
     * L'identifiant métier reçu en path reste operationRef côté API,
     * mais il correspond maintenant à REF_ORDRE côté base.
     */
    @Transactional
    public TransferOperationResponse saveDraft(
            String operationRef,
            SaveTransferDraftRequest request
    ) {
        MvtTrOperation operation = operationLookupService.findByReference(operationRef);

        assertEditable(operation);

        updateOperationHeaderFromDraft(operation, request);
        updateSnapshotsFromDraftIfProvided(operation, request);

        syncHeaderShortcutsFromSnapshots(operation);

        MvtTrOperation saved = operationRepository.save(operation);

        eventService.registerEvent(
                saved,
                OperationEventType.DRAFT_SAVED,
                saved.getStatus(),
                saved.getStatus(),
                request.updatedBy(),
                AGENT_SAISIE_ROLE,
                request.comment(),
                null
        );

        return responseMapper.toResponse(saved);
    }

    private String resolveCorrelationId(TransferCreationContext context) {
        if (notBlank(context.correlationId())) {
            return context.correlationId().trim();
        }

        return "CORR-MS-TR-" + java.util.UUID.randomUUID();
    }

    private Long resolveCodeOperation(
            CreateTransferOrderRequest request,
            TransferCreationContext context
    ) {
        if (request.transferType() == null) {
            throw new IllegalArgumentException("Transfer type is required.");
        }

        return switch (request.transferType()) {
            case C -> 101L;
            case F -> 201L;
        };
    }

    private String clean(String value) {
        return value == null ? null : value.trim();
    }

    private String cleanUpper(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }

    private SwiftPriority parseSwiftPriority(String value) {
        if (value == null || value.isBlank()) {
            return SwiftPriority.N; // valeur par défaut si ton enum contient N
        }

        return SwiftPriority.valueOf(value.trim().toUpperCase());
    }

    private void applyClientData(
            MvtTrOperation operation,
            CreateTransferOrderRequest request
    ) {
        operation.setEndToEndId(clean(request.endToEndId()));

        operation.setMntOrdre(request.orderAmount());
        operation.setCodeDeviseOrdre(cleanUpper(request.orderCurrency()));

        operation.setMntDevise(request.transferAmount());
        operation.setCodeDevise(cleanUpper(request.transferCurrency()));

        operation.setDateValeurTransfert(request.valueDate());

        operation.setCoursConversion(request.fxRate());
        operation.setContreValeurTnd(request.counterValueTnd());

        operation.setSwiftPriority(parseSwiftPriority(request.swiftPriority()));
        operation.setServiceLevelCode(cleanUpper(request.serviceLevelCode()));
        operation.setCategoryPurposeCode(cleanUpper(request.categoryPurposeCode()));

        operation.setPurposeCode(clean(request.purposeCode()));
        operation.setPurposeProprietary(clean(request.purposeProprietary()));
        operation.setRemittanceUnstructured(clean(request.remittanceUnstructured()));

        operation.setChargeBearer(cleanUpper(request.chargeBearer()));

        addPartySnapshot(operation, PartyRole.ULTMT_DBTR, request.ultimateDebtor());
        addPartySnapshot(operation, PartyRole.DBTR, request.debtor());
        addPartySnapshot(operation, PartyRole.CDTR, request.creditor());
        addPartySnapshot(operation, PartyRole.ULTMT_CDTR, request.ultimateCreditor());

        addAccountSnapshot(operation, AccountRole.CDTR_ACCT, request.creditorAccount());
        addAccountSnapshot(operation, AccountRole.CHARGES_ACCT, request.chargesAccount());

        addFinancialAgentSnapshot(
                operation,
                FinancialAgentRole.CDTR_AGT,
                request.creditorAgent()
        );

        syncHeaderShortcutsFromSnapshots(operation);
    }

    private void updateSnapshotsFromDraftIfProvided(
            MvtTrOperation operation,
            SaveTransferDraftRequest request
    ) {
        if (request.ultimateDebtor() != null) {
            removePartiesByRole(operation, PartyRole.ULTMT_DBTR);
            addPartySnapshot(operation, PartyRole.ULTMT_DBTR, request.ultimateDebtor());
        }

        if (request.debtor() != null) {
            removePartiesByRole(operation, PartyRole.DBTR);
            addPartySnapshot(operation, PartyRole.DBTR, request.debtor());
        }

        if (request.creditor() != null) {
            removePartiesByRole(operation, PartyRole.CDTR);
            addPartySnapshot(operation, PartyRole.CDTR, request.creditor());
        }

        if (request.ultimateCreditor() != null) {
            removePartiesByRole(operation, PartyRole.ULTMT_CDTR);
            addPartySnapshot(operation, PartyRole.ULTMT_CDTR, request.ultimateCreditor());
        }

        if (request.creditorAccount() != null) {
            removeAccountsByRole(operation, AccountRole.CDTR_ACCT);
            addAccountSnapshot(operation, AccountRole.CDTR_ACCT, request.creditorAccount());
        }

        if (request.chargesAccount() != null) {
            removeAccountsByRole(operation, AccountRole.CHARGES_ACCT);
            addAccountSnapshot(operation, AccountRole.CHARGES_ACCT, request.chargesAccount());
        }

        if (request.creditorAgent() != null) {
            removeFinancialAgentsByRole(operation, FinancialAgentRole.CDTR_AGT);
            addFinancialAgentSnapshot(operation, FinancialAgentRole.CDTR_AGT, request.creditorAgent());
        }
    }

    /**
     * Mise à jour qualification / montant / devise / motif.
     */
    @Transactional
    public TransferOperationResponse updateQualification(
            String operationRef,
            UpdateTransferQualificationRequest request
    ) {
        MvtTrOperation operation = operationLookupService.findByReference(operationRef);

        assertEditable(operation);

        operation.setMntOrdre(request.orderAmount());
        operation.setCodeDeviseOrdre(request.orderCurrency());
        operation.setMntDevise(request.transferAmount());
        operation.setCodeDevise(request.transferCurrency());
        operation.setPurposeCode(request.purposeCode());
        operation.setPurposeProprietary(request.purposeProprietary());
        operation.setRemittanceUnstructured(request.remittanceUnstructured());
        operation.setChargeBearer(request.chargeBearer());

        MvtTrOperation saved = operationRepository.save(operation);

        registerEvent(
                saved,
                OperationEventType.ORDER_UPDATED,
                saved.getStatus(),
                saved.getStatus(),
                request.updatedBy(),
                AGENT_SAISIE_ROLE,
                request.comment(),
                null
        );

        return responseMapper.toResponse(saved);
    }



    /**
     * Contrôle de toilette.
     */
    @Transactional
    public TransferValidationReport runToiletteControl(
            String operationRef,
            RunToiletteControlRequest request
    ) {
        MvtTrOperation operation = operationLookupService.findByReference(operationRef);

        assertEditable(operation);

        TransferValidationReport report =
                validationService.validateForInputControl(operation);

        validationErrorRepository.deleteByOperation_RefOperation(
                operation.getRefOperation()
        );

        report.errors().forEach(error ->
                validationErrorRepository.save(
                        TrOperationValidationError.builder()
                                .operation(operation)
                                .section(error.section())
                                .fieldPath(error.fieldPath())
                                .errorCode(error.errorCode())
                                .errorMessage(error.errorMessage())
                                .severity(error.severity())
                                .detectedAt(report.controlledAt())
                                .build()
                )
        );

        registerEvent(
                operation,
                OperationEventType.ORDER_UPDATED,
                TransferOperationStatus.X,
                TransferOperationStatus.X,
                request.controlledBy(),
                AGENT_SAISIE_ROLE,
                report.validForSubmission()
                        ? "Toilette control passed"
                        : "Toilette control failed",
                null
        );

        return report;
    }

    /**
     * Consultation des erreurs de validation.
     */
    @Transactional(readOnly = true)
    public List<ValidationErrorDto> getValidationErrors(String operationRef) {
        MvtTrOperation operation = operationLookupService.findByReference(operationRef);

        return validationErrorRepository
                .findByOperation_RefOperationOrderBySectionAscFieldPathAsc(
                        operation.getRefOperation()
                )
                .stream()
                .map(error -> new ValidationErrorDto(
                        error.getSection(),
                        error.getFieldPath(),
                        error.getErrorCode(),
                        error.getErrorMessage(),
                        error.getSeverity()
                ))
                .toList();
    }








    private void updateOperationHeaderFromDraft(
            MvtTrOperation operation,
            SaveTransferDraftRequest request
    ) {
        if (request.orderAmount() != null) {
            operation.setMntOrdre(request.orderAmount());
        }

        if (request.orderCurrency() != null) {
            operation.setCodeDeviseOrdre(request.orderCurrency());
        }

        if (request.transferAmount() != null) {
            operation.setMntDevise(request.transferAmount());
        }

        if (request.transferCurrency() != null) {
            operation.setCodeDevise(request.transferCurrency());
        }

        if (request.valueDate() != null) {
            operation.setDateValeurTransfert(request.valueDate());
        }

        if (request.fxRate() != null) {
            operation.setCoursConversion(request.fxRate());
        }

        if (request.counterValueTnd() != null) {
            operation.setContreValeurTnd(request.counterValueTnd());
        }

        if (request.purposeCode() != null) {
            operation.setPurposeCode(request.purposeCode());
        }

        if (request.purposeProprietary() != null) {
            operation.setPurposeProprietary(request.purposeProprietary());
        }

        if (request.remittanceUnstructured() != null) {
            operation.setRemittanceUnstructured(request.remittanceUnstructured());
        }

        if (request.chargeBearer() != null) {
            operation.setChargeBearer(request.chargeBearer());
        }

        if (request.swiftPriority() != null) {
            operation.setSwiftPriority(request.swiftPriority());
        }

        if (request.serviceLevelCode() != null) {
            operation.setServiceLevelCode(request.serviceLevelCode());
        }

        if (request.localInstrumentCode() != null) {
            operation.setLocalInstrumentCode(request.localInstrumentCode());
        }

        if (request.categoryPurposeCode() != null) {
            operation.setCategoryPurposeCode(request.categoryPurposeCode());
        }
    }


    @Transactional
    public TransferOperationResponse updateDebtor(
            String operationRef,
            UpdateDebtorRequest request
    ) {
        MvtTrOperation operation = operationLookupService.findByReference(operationRef);

        assertEditable(operation);

        removePartiesByRole(
                operation,
                PartyRole.ULTMT_DBTR,
                PartyRole.DBTR
        );

        removeAccountsByRole(
                operation,
                AccountRole.CHARGES_ACCT
        );

        addPartySnapshot(operation, PartyRole.ULTMT_DBTR, request.ultimateDebtor());
        addPartySnapshot(operation, PartyRole.DBTR, request.debtor());
        addAccountSnapshot(operation, AccountRole.CHARGES_ACCT, request.chargesAccount());

        syncHeaderShortcutsFromSnapshots(operation);

        MvtTrOperation saved = operationRepository.save(operation);

        eventService.registerEvent(
                saved,
                OperationEventType.ORDER_UPDATED,
                saved.getStatus(),
                saved.getStatus(),
                request.updatedBy(),
                AGENT_SAISIE_ROLE,
                request.comment(),
                null
        );

        return responseMapper.toResponse(saved);
    }

    private void removePartiesByRole(
            MvtTrOperation operation,
            PartyRole... roles
    ) {
        if (operation.getParties() == null) {
            return;
        }

        List<PartyRole> roleList = Arrays.asList(roles);

        operation.getParties()
                .removeIf(party -> roleList.contains(party.getPartyRole()));
    }

    private void removeAccountsByRole(
            MvtTrOperation operation,
            AccountRole... roles
    ) {
        if (operation.getAccounts() == null) {
            return;
        }

        List<AccountRole> roleList = Arrays.asList(roles);

        operation.getAccounts()
                .removeIf(account -> roleList.contains(account.getAccountRole()));
    }

    private void removeFinancialAgentsByRole(
            MvtTrOperation operation,
            FinancialAgentRole... roles
    ) {
        if (operation.getFinancialAgents() == null) {
            return;
        }

        List<FinancialAgentRole> roleList = Arrays.asList(roles);

        operation.getFinancialAgents()
                .removeIf(agent -> roleList.contains(agent.getAgentRole()));
    }

    @Transactional
    public TransferOperationResponse updateCreditor(
            String operationRef,
            UpdateCreditorRequest request
    ) {
        MvtTrOperation operation = operationLookupService.findByReference(operationRef);

        assertEditable(operation);

        removePartiesByRole(
                operation,
                PartyRole.CDTR,
                PartyRole.ULTMT_CDTR
        );

        removeAccountsByRole(
                operation,
                AccountRole.CDTR_ACCT
        );

        removeFinancialAgentsByRole(
                operation,
                FinancialAgentRole.CDTR_AGT
        );

        addPartySnapshot(operation, PartyRole.CDTR, request.creditor());
        addPartySnapshot(operation, PartyRole.ULTMT_CDTR, request.ultimateCreditor());
        addAccountSnapshot(operation, AccountRole.CDTR_ACCT, request.creditorAccount());
        addFinancialAgentSnapshot(operation, FinancialAgentRole.CDTR_AGT, request.creditorAgent());

        syncHeaderShortcutsFromSnapshots(operation);

        MvtTrOperation saved = operationRepository.save(operation);

        eventService.registerEvent(
                saved,
                OperationEventType.ORDER_UPDATED,
                saved.getStatus(),
                saved.getStatus(),
                request.updatedBy(),
                AGENT_SAISIE_ROLE,
                request.comment(),
                null
        );

        return responseMapper.toResponse(saved);
    }

    private void syncHeaderShortcutsFromSnapshots(MvtTrOperation operation) {
        TrParty ultimateDebtor = findParty(operation, PartyRole.ULTMT_DBTR);
        TrParty debtor = findParty(operation, PartyRole.DBTR);
        TrParty creditor = findParty(operation, PartyRole.CDTR);
        TrParty ultimateCreditor = findParty(operation, PartyRole.ULTMT_CDTR);

        TrAccount creditorAccount = findAccount(operation, AccountRole.CDTR_ACCT);
        TrAccount chargesAccount = findAccount(operation, AccountRole.CHARGES_ACCT);

        operation.setUltimateDebtorId(extractNumericLocalPartyId(ultimateDebtor));
        operation.setDebtorId(extractNumericLocalPartyId(debtor));
        operation.setCreditorId(extractNumericLocalPartyId(creditor));
        operation.setUltimateCreditorId(extractNumericLocalPartyId(ultimateCreditor));

        operation.setNoCompteCreditor(resolveAccountReference(creditorAccount));
        operation.setNoCompteCommission(resolveAccountReference(chargesAccount));
    }

    private Long extractNumericLocalPartyId(TrParty party) {
        if (party == null || party.getLocalPartyId() == null || party.getLocalPartyId().isBlank()) {
            return null;
        }

        try {
            return Long.valueOf(party.getLocalPartyId());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String resolveAccountReference(TrAccount account) {
        if (account == null) {
            return null;
        }

        if (notBlank(account.getIban())) {
            return account.getIban();
        }

        if (notBlank(account.getRibLocal())) {
            return account.getRibLocal();
        }

        if (notBlank(account.getCoreAccountId())) {
            return account.getCoreAccountId();
        }

        if (notBlank(account.getOtherAccountId())) {
            return account.getOtherAccountId();
        }

        return null;
    }

    private TrParty findParty(MvtTrOperation operation, PartyRole role) {
        if (operation.getParties() == null) {
            return null;
        }

        return operation.getParties()
                .stream()
                .filter(party -> party.getPartyRole() == role)
                .findFirst()
                .orElse(null);
    }

    private TrAccount findAccount(MvtTrOperation operation, AccountRole role) {
        if (operation.getAccounts() == null) {
            return null;
        }

        return operation.getAccounts()
                .stream()
                .filter(account -> account.getAccountRole() == role)
                .findFirst()
                .orElse(null);
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }



    private void assertEditable(MvtTrOperation operation) {
        if (!operation.isEditable()) {
            throw new IllegalStateException(
                    "Only operations with status X / En cours can be modified. Current status: "
                            + operation.getStatus()
            );
        }
    }

    private void addPartySnapshot(
            MvtTrOperation operation,
            PartyRole role,
            PartyDto dto
    ) {
        if (dto == null) {
            return;
        }

        TrParty party = new TrParty();
        party.setPartyRole(role);
        party.setPartyType(dto.partyType());
        party.setName(dto.name());
        party.setLocalPartyId(dto.localPartyId());
        party.setLocalIdType(dto.localIdType());
        party.setCustomerCode(dto.customerCode());
        party.setCountryOfResidence(dto.countryOfResidence());
        party.setBirthDate(dto.birthDate());
        party.setCityOfBirth(dto.cityOfBirth());
        party.setCountryOfBirth(dto.countryOfBirth());

        if (dto.postalAddresses() != null) {
            List<TrPartyPostalAddress> addresses = dto.postalAddresses()
                    .stream()
                    .map(addressDto -> toPostalAddressEntity(party, addressDto))
                    .toList();

            party.setPostalAddresses(new ArrayList<>(addresses));
        }

        if (dto.identifications() != null) {
            List<TrPartyIdentification> identifications = dto.identifications()
                    .stream()
                    .map(identificationDto -> toIdentificationEntity(party, identificationDto))
                    .toList();

            party.setIdentifications(new ArrayList<>(identifications));
        }

        operation.addParty(party);
    }

    private TrPartyPostalAddress toPostalAddressEntity(
            TrParty party,
            PostalAddressDto dto
    ) {
        TrPartyPostalAddress address = new TrPartyPostalAddress();

        address.setParty(party);
        address.setAddressType(dto.addressType());
        address.setStreetName(dto.streetName());
        address.setBuildingNumber(dto.buildingNumber());
        address.setPostCode(dto.postCode());
        address.setTownName(dto.townName());
        address.setCountrySubDivision(dto.countrySubDivision());
        address.setCountry(dto.country());
        address.setAddressLine1(dto.addressLine1());
        address.setAddressLine2(dto.addressLine2());
        address.setAddressLine3(dto.addressLine3());

        return address;
    }

    private TrPartyIdentification toIdentificationEntity(
            TrParty party,
            PartyIdentificationDto dto
    ) {
        TrPartyIdentification identification = new TrPartyIdentification();

        identification.setParty(party);
        identification.setIdentificationScope(normalizeIdentificationScope(dto.identificationScope()));
        identification.setIdentificationType(dto.identificationType());
        identification.setIdentificationValue(dto.identificationValue());
        identification.setIssuer(dto.issuer());
        identification.setSchemeNameCode(dto.schemeNameCode());
        identification.setSchemeNameProprietary(dto.schemeNameProprietary());

        return identification;
    }

    private String normalizeIdentificationScope(String scope) {
        if (scope == null || scope.isBlank()) {
            return "LOCAL";
        }

        return scope.trim().toUpperCase();
    }

    private void addAccountSnapshot(
            MvtTrOperation operation,
            AccountRole role,
            AccountDto dto
    ) {
        if (dto == null) {
            return;
        }

        TrAccount account = new TrAccount();

        account.setAccountRole(role);
        account.setIban(dto.iban());
        account.setOtherAccountId(dto.otherAccountId());
        account.setAccountScheme(dto.accountScheme());
        account.setAccountCurrency(dto.accountCurrency());
        account.setAccountName(dto.accountName());
        account.setCoreAccountId(dto.coreAccountId());
        account.setRibLocal(dto.ribLocal());

        operation.addAccount(account);
    }

    private void addFinancialAgentSnapshot(
            MvtTrOperation operation,
            FinancialAgentRole role,
            FinancialAgentDto dto
    ) {
        if (dto == null) {
            return;
        }

        TrFinancialAgent agent = new TrFinancialAgent();

        agent.setAgentRole(role);
        agent.setBicfi(dto.bicfi());
        agent.setLei(dto.lei());
        agent.setClearingSystemCode(dto.clearingSystemCode());
        agent.setClearingMemberId(dto.clearingMemberId());
        agent.setAgentName(dto.agentName());
        agent.setBranchId(dto.branchId());
        agent.setBranchName(dto.branchName());
        agent.setCountry(dto.country());
        agent.setAddressLine1(dto.addressLine1());
        agent.setAddressLine2(dto.addressLine2());
        agent.setTownName(dto.townName());

        operation.addFinancialAgent(agent);
    }


    private void registerEvent(
            MvtTrOperation operation,
            OperationEventType eventType,
            TransferOperationStatus oldStatus,
            TransferOperationStatus newStatus,
            String actorUserId,
            String actorRole,
            String comment,
            String eventPayload
    ) {
        eventService.registerEvent(
                operation,
                eventType,
                oldStatus,
                newStatus,
                actorUserId,
                actorRole,
                comment,
                eventPayload
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}