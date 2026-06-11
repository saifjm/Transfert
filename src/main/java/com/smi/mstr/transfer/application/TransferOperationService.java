package com.smi.mstr.transfer.application;

import com.smi.mstr.transfer.application.mapper.TransferOperationResponseMapper;
import com.smi.mstr.transfer.application.mapper.TransferOrderDataMapper;
import com.smi.mstr.transfer.domain.entity.*;
import com.smi.mstr.transfer.domain.enums.AccountRole;
import com.smi.mstr.transfer.domain.enums.CompletionStatus;
import com.smi.mstr.transfer.domain.enums.FinancialAgentRole;
import com.smi.mstr.transfer.domain.enums.OperationEventType;
import com.smi.mstr.transfer.domain.enums.OriginChannel;
import com.smi.mstr.transfer.domain.enums.PartyRole;
import com.smi.mstr.transfer.domain.enums.TransferOperationStatus;
import com.smi.mstr.transfer.domain.repository.MvtTrOperationRepository;
import com.smi.mstr.transfer.domain.repository.TrOperationEventRepository;
import com.smi.mstr.transfer.domain.repository.TrOperationValidationErrorRepository;
import com.smi.mstr.transfer.dto.*;
import com.smi.mstr.transfer.dto.normalized.AccountDto;
import com.smi.mstr.transfer.dto.normalized.FinancialAgentDto;
import com.smi.mstr.transfer.dto.normalized.PartyDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferOperationService {

    private static final String AGENT_SAISIE_ROLE = "AGENT_SAISIE";

    private final MvtTrOperationRepository operationRepository;
    private final TrOperationEventRepository eventRepository;
    private final TransferReferenceService referenceService;
    private final TransferOrderDataMapper orderDataMapper;
    private final TransferOperationResponseMapper responseMapper;
    private final TransferOrderValidationService validationService;
    private final TrOperationValidationErrorRepository validationErrorRepository;

    @Transactional
    public TransferOperationResponse createManualOrder(CreateTransferOrderRequest request) {
        String operationRef = referenceService.generateReference();
        referenceService.ensureUnique(operationRef);

        MvtTrOperation operation = buildNewManualOperation(request, operationRef);

        attachNormalizedOrderData(
                operation,
                request.debtor(),
                request.debtorAccount(),
                request.creditor(),
                request.creditorAccount(),
                request.creditorAgent()
        );

        operation.setCompletionStatus(resolveCompletionStatus(operation));

        MvtTrOperation saved = operationRepository.save(operation);

        registerEvent(
                saved,
                OperationEventType.OPERATION_CREATED,
                null,
                TransferOperationStatus.X,
                request.createdBy(),
                AGENT_SAISIE_ROLE,
                "Manual normalized transfer order created",
                null
        );

        return responseMapper.toResponse(saved);
    }

    @Transactional
    public TransferOperationResponse saveDraft(String operationRef, SaveTransferDraftRequest request) {
        MvtTrOperation operation = findOperationByRef(operationRef);

        assertEditable(operation);

        updateOperationHeaderFromDraft(operation, request);

        operation.clearNormalizedOrderData();

        attachNormalizedOrderData(
                operation,
                request.debtor(),
                request.debtorAccount(),
                request.creditor(),
                request.creditorAccount(),
                request.creditorAgent()
        );

        operation.setCompletionStatus(resolveCompletionStatus(operation));

        MvtTrOperation saved = operationRepository.save(operation);

        registerEvent(
                saved,
                OperationEventType.DRAFT_SAVED,
                TransferOperationStatus.X,
                TransferOperationStatus.X,
                request.updatedBy(),
                AGENT_SAISIE_ROLE,
                request.comment(),
                null
        );

        return responseMapper.toResponse(saved);
    }

    @Transactional
    public TransferOperationResponse updateDebtor(
            String operationRef,
            UpdateDebtorRequest request
    ) {
        MvtTrOperation operation = findOperationByRef(operationRef);
        assertEditable(operation);

        operation.removeAccountsByRole(AccountRole.DBTR_ACCT);
        operation.removePartiesByRole(PartyRole.DBTR);

        TrParty debtor = orderDataMapper.toParty(request.debtor(), PartyRole.DBTR);
        operation.addParty(debtor);

        operation.addAccount(orderDataMapper.toAccount(
                request.debtorAccount(),
                AccountRole.DBTR_ACCT,
                debtor
        ));

        operation.setUpdatedAt(LocalDateTime.now());
        operation.setCompletionStatus(resolveCompletionStatus(operation));

        MvtTrOperation saved = operationRepository.save(operation);

        registerEvent(
                saved,
                OperationEventType.ORDER_UPDATED,
                TransferOperationStatus.X,
                TransferOperationStatus.X,
                request.updatedBy(),
                AGENT_SAISIE_ROLE,
                request.comment(),
                null
        );

        return responseMapper.toResponse(saved);
    }

    @Transactional
    public TransferOperationResponse updateCreditor(
            String operationRef,
            UpdateCreditorRequest request
    ) {
        MvtTrOperation operation = findOperationByRef(operationRef);
        assertEditable(operation);

        operation.removeAccountsByRole(AccountRole.CDTR_ACCT);
        operation.removeFinancialAgentsByRole(FinancialAgentRole.CDTR_AGT);
        operation.removePartiesByRole(PartyRole.CDTR);

        TrParty creditor = orderDataMapper.toParty(request.creditor(), PartyRole.CDTR);
        operation.addParty(creditor);

        operation.addAccount(orderDataMapper.toAccount(
                request.creditorAccount(),
                AccountRole.CDTR_ACCT,
                creditor
        ));

        operation.addFinancialAgent(orderDataMapper.toFinancialAgent(
                request.creditorAgent(),
                FinancialAgentRole.CDTR_AGT
        ));

        operation.setUpdatedAt(LocalDateTime.now());
        operation.setCompletionStatus(resolveCompletionStatus(operation));

        MvtTrOperation saved = operationRepository.save(operation);

        registerEvent(
                saved,
                OperationEventType.ORDER_UPDATED,
                TransferOperationStatus.X,
                TransferOperationStatus.X,
                request.updatedBy(),
                AGENT_SAISIE_ROLE,
                request.comment(),
                null
        );

        return responseMapper.toResponse(saved);
    }

    @Transactional
    public TransferOperationResponse updateQualification(
            String operationRef,
            UpdateTransferQualificationRequest request
    ) {
        MvtTrOperation operation = findOperationByRef(operationRef);
        assertEditable(operation);

        operation.setOrderAmount(request.orderAmount());
        operation.setOrderCurrency(request.orderCurrency());
        operation.setTransferAmount(request.transferAmount());
        operation.setTransferCurrency(request.transferCurrency());
        operation.setPurposeCode(request.purposeCode());
        operation.setPurposeProprietary(request.purposeProprietary());
        operation.setRemittanceUnstructured(request.remittanceUnstructured());
        operation.setChargeBearer(request.chargeBearer());
        operation.setUpdatedAt(LocalDateTime.now());

        operation.setCompletionStatus(resolveCompletionStatus(operation));

        MvtTrOperation saved = operationRepository.save(operation);

        registerEvent(
                saved,
                OperationEventType.ORDER_UPDATED,
                TransferOperationStatus.X,
                TransferOperationStatus.X,
                request.updatedBy(),
                AGENT_SAISIE_ROLE,
                request.comment(),
                null
        );

        return responseMapper.toResponse(saved);
    }

    @Transactional
    public TransferValidationReport runToiletteControl(
            String operationRef,
            RunToiletteControlRequest request
    ) {
        MvtTrOperation operation = findOperationByRef(operationRef);
        assertEditable(operation);

        TransferValidationReport report =
                validationService.validateForInputControl(operation);

        validationErrorRepository.deleteByOperation_RefOperation(operation.getRefOperation());

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


    @Transactional(readOnly = true)
    public List<ValidationErrorDto> getValidationErrors(String operationRef) {
        findOperationByRef(operationRef);

        return validationErrorRepository
                .findByOperation_OperationRefOrderBySectionAscFieldPathAsc(operationRef)
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



    private MvtTrOperation buildNewManualOperation(
            CreateTransferOrderRequest request,
            String operationRef
    ) {
        return MvtTrOperation.builder()
                .operationRef(operationRef)
                .dateOperation(LocalDate.now())
                .status(TransferOperationStatus.X)
                .completionStatus(CompletionStatus.EMPTY)
                .transferType(request.transferType())
                .swiftPriority(request.swiftPriority())
                .numDossier(request.numDossier())
                .dateDossier(request.dateDossier())
                .branchCode(request.branchCode())
                .createdBy(request.createdBy())
                .sourceChannel(OriginChannel.AGENCY)
                .orderAmount(request.orderAmount())
                .orderCurrency(request.orderCurrency())
                .transferAmount(request.transferAmount())
                .transferCurrency(request.transferCurrency())
                .purposeCode(request.purposeCode())
                .purposeProprietary(request.purposeProprietary())
                .remittanceUnstructured(request.remittanceUnstructured())
                .chargeBearer(request.chargeBearer())
                .createdAt(LocalDateTime.now())
                .build();
    }

    private void updateOperationHeaderFromDraft(
            MvtTrOperation operation,
            SaveTransferDraftRequest request
    ) {
        operation.setOrderAmount(request.orderAmount());
        operation.setOrderCurrency(request.orderCurrency());
        operation.setTransferAmount(request.transferAmount());
        operation.setTransferCurrency(request.transferCurrency());
        operation.setPurposeCode(request.purposeCode());
        operation.setPurposeProprietary(request.purposeProprietary());
        operation.setRemittanceUnstructured(request.remittanceUnstructured());
        operation.setChargeBearer(request.chargeBearer());
        operation.setUpdatedAt(LocalDateTime.now());
    }

    private void attachNormalizedOrderData(
            MvtTrOperation operation,
            PartyDto debtorDto,
            AccountDto debtorAccountDto,
            PartyDto creditorDto,
            AccountDto creditorAccountDto,
            FinancialAgentDto creditorAgentDto
    ) {
        TrParty debtor = orderDataMapper.toParty(debtorDto, PartyRole.DBTR);
        TrParty creditor = orderDataMapper.toParty(creditorDto, PartyRole.CDTR);

        operation.addParty(debtor);
        operation.addParty(creditor);

        operation.addAccount(orderDataMapper.toAccount(
                debtorAccountDto,
                AccountRole.DBTR_ACCT,
                debtor
        ));

        operation.addAccount(orderDataMapper.toAccount(
                creditorAccountDto,
                AccountRole.CDTR_ACCT,
                creditor
        ));

        operation.addFinancialAgent(orderDataMapper.toFinancialAgent(
                creditorAgentDto,
                FinancialAgentRole.CDTR_AGT
        ));
    }

    private CompletionStatus resolveCompletionStatus(MvtTrOperation operation) {
        boolean hasAmount = operation.getTransferAmount() != null
                && notBlank(operation.getTransferCurrency());

        boolean hasDebtor = hasParty(operation, PartyRole.DBTR);
        boolean hasCreditor = hasParty(operation, PartyRole.CDTR);

        boolean hasDebtorAccount = hasAccount(operation, AccountRole.DBTR_ACCT);
        boolean hasCreditorAccount = hasAccount(operation, AccountRole.CDTR_ACCT);

        boolean hasCreditorAgent = hasFinancialAgent(operation, FinancialAgentRole.CDTR_AGT);

        if (hasAmount
                && hasDebtor
                && hasCreditor
                && hasDebtorAccount
                && hasCreditorAccount
                && hasCreditorAgent) {
            return CompletionStatus.COMPLETE;
        }

        if (hasAmount
                || hasDebtor
                || hasCreditor
                || hasDebtorAccount
                || hasCreditorAccount
                || hasCreditorAgent) {
            return CompletionStatus.PARTIAL;
        }

        return CompletionStatus.EMPTY;
    }

    private boolean hasParty(MvtTrOperation operation, PartyRole role) {
        return operation.getParties() != null
                && operation.getParties()
                .stream()
                .anyMatch(party ->
                        party.getPartyRole() == role
                                && notBlank(party.getName())
                );
    }

    private boolean hasAccount(MvtTrOperation operation, AccountRole role) {
        return operation.getAccounts() != null
                && operation.getAccounts()
                .stream()
                .anyMatch(account ->
                        account.getAccountRole() == role
                                && hasAnyAccountIdentifier(account)
                );
    }

    private boolean hasFinancialAgent(MvtTrOperation operation, FinancialAgentRole role) {
        return operation.getFinancialAgents() != null
                && operation.getFinancialAgents()
                .stream()
                .anyMatch(agent ->
                        agent.getAgentRole() == role
                                && (
                                notBlank(agent.getBicfi())
                                        || notBlank(agent.getClearingMemberId())
                                        || notBlank(agent.getAgentName())
                        )
                );
    }

    private boolean hasAnyAccountIdentifier(TrAccount account) {
        return notBlank(account.getIban())
                || notBlank(account.getOtherAccountId())
                || notBlank(account.getCoreAccountId())
                || notBlank(account.getRibLocal());
    }

    private MvtTrOperation findOperationByRef(String operationRef) {
        return operationRepository.findByOperationRef(operationRef)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transfer operation not found: " + operationRef
                ));
    }

    private void assertEditable(MvtTrOperation operation) {
        if (!operation.isEditable()) {
            throw new IllegalStateException(
                    "Only operations with status X / En cours can be modified. Current status: "
                            + operation.getStatus()
            );
        }
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
        TrOperationEvent event = TrOperationEvent.builder()
                .operation(operation)
                .eventType(eventType)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .actorUserId(actorUserId)
                .actorRole(actorRole)
                .actionAt(LocalDateTime.now())
                .commentText(comment)
                .eventPayload(eventPayload)
                .build();

        eventRepository.save(event);
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}