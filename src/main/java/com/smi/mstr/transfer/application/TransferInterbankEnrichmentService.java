package com.smi.mstr.transfer.application;

import com.smi.mstr.transfer.application.ref.*;
import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.domain.entity.TrFinancialAgent;
import com.smi.mstr.transfer.domain.entity.TrInterbankEnrichment;
import com.smi.mstr.transfer.domain.entity.TrInterbankInstruction;
import com.smi.mstr.transfer.domain.enums.*;
import com.smi.mstr.transfer.domain.repository.TrInterbankEnrichmentRepository;
import com.smi.mstr.transfer.domain.repository.TrInterbankInstructionRepository;
import com.smi.mstr.transfer.dto.interbank.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferInterbankEnrichmentService {

    private static final String VALIDATOR_ROLE = "VALIDATOR";

    private final TransferOperationLookupService operationLookupService;
    private final TrInterbankEnrichmentRepository enrichmentRepository;
    private final TrInterbankInstructionRepository instructionRepository;
    private final TransferOperationEventService eventService;
    private final MsRefInterbankClient msRefInterbankClient;
    private final MsRefClientProperties msRefClientProperties;
    private final tools.jackson.databind.ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public InterbankEnrichmentResponse getEnrichment(String operationRef) {
        MvtTrOperation operation = operationLookupService.findByReference(operationRef);
        TrInterbankEnrichment enrichment = findOrBuildDefault(operation);

        List<TrInterbankInstruction> instructions =
                instructionRepository.findByOperation_RefOperationOrderByInstructionIdAsc(
                        operation.getRefOperation()
                );

        return toResponse(operation, enrichment, instructions, List.of());
    }

    @Transactional
    public InterbankEnrichmentResponse fetchDefaultChain(
            String operationRef,
            FetchDefaultInterbankChainRequest request
    ) {
        MvtTrOperation operation = operationLookupService.findByReference(operationRef);

        assertValidatorActionAllowed(operation);

        RefInterbankChainRequest refRequest = buildRefRequest(operation);

        RefInterbankChainResponse refResponse =
                msRefInterbankClient.getDefaultInterbankChain(refRequest);

        removeFinancialAgentsByRole(
                operation,
                FinancialAgentRole.INSTG_AGT,
                FinancialAgentRole.INSTD_AGT,
                FinancialAgentRole.DBTR_AGT,
                FinancialAgentRole.CDTR_AGT,
                FinancialAgentRole.INTRMY_AGT_1,
                FinancialAgentRole.INTRMY_AGT_2,
                FinancialAgentRole.INTRMY_AGT_3,
                FinancialAgentRole.NOSTRO_AGT,
                FinancialAgentRole.REIMBURSEMENT_AGT,
                FinancialAgentRole.SENDER_CORRESPONDENT,
                FinancialAgentRole.RECEIVER_CORRESPONDENT
        );

        refResponse.agents()
                .forEach(agent -> addFinancialAgentSnapshot(operation, toInterbankAgentDto(agent)));

        TrInterbankEnrichment enrichment = getOrCreateEnrichment(operation);

        enrichment.setPaymentRouteType(refResponse.paymentRouteType());
        enrichment.setSettlementMethod(refResponse.settlementMethod());
        enrichment.setSettlementAccountRef(refResponse.settlementAccountRef());
        enrichment.setSettlementCurrency(refResponse.settlementCurrency());
        enrichment.setSettlementAmount(refResponse.settlementAmount());
        enrichment.setSettlementDate(refResponse.settlementDate());

        enrichment.setNostroAccountRef(refResponse.nostroAccountRef());
        enrichment.setNostroCurrency(refResponse.nostroCurrency());
        enrichment.setNostroAgentBic(refResponse.nostroAgentBic());

        enrichment.setCoverRequired(refResponse.coverRequired() ? "O" : "N");
        enrichment.setCoverMessageType(refResponse.coverMessageType());
        enrichment.setCoverReason(refResponse.coverReason());

        enrichment.setEnrichmentStatus(InterbankEnrichmentStatus.PARTIALLY_ENRICHED);
        enrichment.setPaymentPathStatus(resolvePaymentPathStatus(operation));

        enrichment.setRefProposalId(refResponse.proposalId());
        enrichment.setRefSourceSystem(resolveRefSourceSystem());
        enrichment.setRefVersion(refResponse.refVersion());
        enrichment.setRefFetchedAt(LocalDateTime.now());
        enrichment.setManualOverride("N");

        enrichment.setDefaultChainJson(toJson(refResponse));
        enrichment.setFinalChainJson(toJson(refResponse));

        enrichment.setEnrichedBy(request.requestedBy());
        enrichment.setEnrichedAt(LocalDateTime.now());

        enrichmentRepository.save(enrichment);

        eventService.registerEvent(
                operation,
                OperationEventType.INTERBANK_DEFAULT_CHAIN_FETCHED,
                operation.getStatus(),
                operation.getStatus(),
                request.requestedBy(),
                VALIDATOR_ROLE,
                "Default interbank chain fetched from MS-REF",
                null
        );

        List<TrInterbankInstruction> instructions =
                instructionRepository.findByOperation_RefOperationOrderByInstructionIdAsc(
                        operation.getRefOperation()
                );

        return toResponse(operation, enrichment, instructions, List.of());
    }

    @Transactional
    public InterbankEnrichmentResponse savePaymentPath(
            String operationRef,
            SavePaymentPathRequest request
    ) {
        MvtTrOperation operation = operationLookupService.findByReference(operationRef);

        assertValidatorActionAllowed(operation);

        validatePaymentPathRequest(request);

        removeFinancialAgentsByRole(
                operation,
                FinancialAgentRole.DBTR_AGT,
                FinancialAgentRole.CDTR_AGT,
                FinancialAgentRole.INTRMY_AGT_1,
                FinancialAgentRole.INTRMY_AGT_2,
                FinancialAgentRole.INTRMY_AGT_3,
                FinancialAgentRole.REIMBURSEMENT_AGT,
                FinancialAgentRole.NOSTRO_AGT,
                FinancialAgentRole.SENDER_CORRESPONDENT,
                FinancialAgentRole.RECEIVER_CORRESPONDENT
        );

        request.agents().forEach(agentDto -> addFinancialAgentSnapshot(operation, agentDto));

        TrInterbankEnrichment enrichment = getOrCreateEnrichment(operation);
        enrichment.setPaymentPathStatus(resolvePaymentPathStatus(operation));
        enrichment.setEnrichmentStatus(resolveEnrichmentStatus(enrichment.getPaymentPathStatus()));
        enrichment.setEnrichedBy(request.updatedBy());
        enrichment.setEnrichedAt(LocalDateTime.now());

        enrichmentRepository.save(enrichment);

        eventService.registerEvent(
                operation,
                OperationEventType.INTERBANK_AGENT_UPDATED,
                operation.getStatus(),
                operation.getStatus(),
                request.updatedBy(),
                VALIDATOR_ROLE,
                request.comment(),
                null
        );

        List<TrInterbankInstruction> instructions =
                instructionRepository.findByOperation_RefOperationOrderByInstructionIdAsc(
                        operation.getRefOperation()
                );

        return toResponse(operation, enrichment, instructions, List.of());
    }

    @Transactional
    public InterbankEnrichmentResponse saveInstructions(
            String operationRef,
            SaveInterbankInstructionsRequest request
    ) {
        MvtTrOperation operation = operationLookupService.findByReference(operationRef);

        assertValidatorActionAllowed(operation);

        validateInstructions(request);

        instructionRepository.deleteByOperation_RefOperation(operation.getRefOperation());

        for (InterbankInstructionDto dto : request.instructions()) {
            TrInterbankInstruction instruction = TrInterbankInstruction.builder()
                    .operation(operation)
                    .instructionType(dto.instructionType())
                    .targetFormat(dto.targetFormat())
                    .targetAgentRole(dto.targetAgentRole())
                    .instructionCode(clean(dto.instructionCode()))
                    .instructionText(clean(dto.instructionText()))
                    .createdBy(request.updatedBy())
                    .createdAt(LocalDateTime.now())
                    .build();

            instructionRepository.save(instruction);
        }

        TrInterbankEnrichment enrichment = getOrCreateEnrichment(operation);
        enrichment.setEnrichmentStatus(InterbankEnrichmentStatus.PARTIALLY_ENRICHED);
        enrichment.setEnrichedBy(request.updatedBy());
        enrichment.setEnrichedAt(LocalDateTime.now());
        enrichmentRepository.save(enrichment);

        eventService.registerEvent(
                operation,
                OperationEventType.INTERBANK_ENRICHMENT_REQUESTED,
                operation.getStatus(),
                operation.getStatus(),
                request.updatedBy(),
                VALIDATOR_ROLE,
                request.comment(),
                null
        );

        List<TrInterbankInstruction> instructions =
                instructionRepository.findByOperation_RefOperationOrderByInstructionIdAsc(
                        operation.getRefOperation()
                );

        return toResponse(operation, enrichment, instructions, List.of());
    }

    @Transactional
    public InterbankEnrichmentResponse determineCover(
            String operationRef,
            DetermineCoverRequest request
    ) {
        MvtTrOperation operation = operationLookupService.findByReference(operationRef);

        assertValidatorActionAllowed(operation);

        TrInterbankEnrichment enrichment = getOrCreateEnrichment(operation);

        CoverDecision decision = determineCoverDecision(operation, request);

        enrichment.setPaymentRouteType(decision.paymentRouteType());
        enrichment.setCoverRequired(decision.coverRequired() ? "O" : "N");
        enrichment.setCoverMessageType(decision.coverMessageType());
        enrichment.setCoverReason(decision.reason());

        enrichment.setSettlementMethod(request.settlementMethod());
        enrichment.setSettlementAccountRef(clean(request.settlementAccountRef()));
        enrichment.setSettlementCurrency(cleanUpper(request.settlementCurrency()));
        enrichment.setSettlementAmount(request.settlementAmount());
        enrichment.setSettlementDate(request.settlementDate());

        enrichment.setNostroAccountRef(clean(request.nostroAccountRef()));
        enrichment.setNostroCurrency(cleanUpper(request.nostroCurrency()));
        enrichment.setNostroAgentBic(cleanUpper(request.nostroAgentBic()));

        enrichment.setEnrichmentStatus(InterbankEnrichmentStatus.PARTIALLY_ENRICHED);
        enrichment.setEnrichedBy(request.requestedBy());
        enrichment.setEnrichedAt(LocalDateTime.now());

        enrichmentRepository.save(enrichment);

        eventService.registerEvent(
                operation,
                OperationEventType.NOSTRO_SELECTED,
                operation.getStatus(),
                operation.getStatus(),
                request.requestedBy(),
                VALIDATOR_ROLE,
                decision.reason(),
                null
        );

        List<TrInterbankInstruction> instructions =
                instructionRepository.findByOperation_RefOperationOrderByInstructionIdAsc(
                        operation.getRefOperation()
                );

        return toResponse(operation, enrichment, instructions, List.of());
    }

    @Transactional
    public InterbankControlReport controlCorrespondents(String operationRef) {
        MvtTrOperation operation = operationLookupService.findByReference(operationRef);

        List<InterbankValidationErrorDto> errors = new ArrayList<>();

        validateCorrespondents(operation, errors);

        boolean valid = errors.stream()
                .noneMatch(e -> e.severity() == ValidationSeverity.BLOCKING);

        TrInterbankEnrichment enrichment = getOrCreateEnrichment(operation);
        enrichment.setLastControlStatus(valid ? "VALID" : "INVALID");
        enrichment.setLastControlAt(LocalDateTime.now());
        enrichment.setPaymentPathStatus(valid ? PaymentPathStatus.COMPLETE : PaymentPathStatus.INVALID);
        enrichment.setEnrichmentStatus(valid
                ? InterbankEnrichmentStatus.ENRICHED
                : InterbankEnrichmentStatus.MANUAL_REVIEW_REQUIRED);

        enrichmentRepository.save(enrichment);

        eventService.registerEvent(
                operation,
                valid
                        ? OperationEventType.INTERBANK_ENRICHMENT_COMPLETED
                        : OperationEventType.INTERBANK_MANUAL_REVIEW_REQUIRED,
                operation.getStatus(),
                operation.getStatus(),
                "SYSTEM",
                "SYSTEM",
                valid ? "Interbank correspondent control passed"
                        : "Interbank correspondent control failed",
                null
        );

        return new InterbankControlReport(
                operation.getRefOrdre(),
                valid,
                LocalDateTime.now(),
                errors
        );
    }

    private String resolveRefSourceSystem() {
        return msRefClientProperties.mode() == MsRefMode.DUMMY
                ? "MS-REF-DUMMY"
                : "MS-REF";
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return null;
        }
    }

    private void validateSettlementData(
            MvtTrOperation operation,
            TrInterbankEnrichment enrichment,
            List<InterbankValidationErrorDto> errors
    ) {
        if (isBlank(operation.getChargeBearer())) {
            addError(errors,
                    "chargeBearer",
                    "CHARGE_BEARER_REQUIRED",
                    "Charge bearer is required before SWIFT emission.",
                    ValidationSeverity.BLOCKING);
        }

        if (enrichment.getPaymentRouteType() == PaymentRouteType.COVER_REQUIRED) {
            if (!"O".equals(enrichment.getCoverRequired())) {
                addError(errors,
                        "coverRequired",
                        "COVER_REQUIRED_INCONSISTENT",
                        "Cover flag must be O when payment route type is COVER_REQUIRED.",
                        ValidationSeverity.BLOCKING);
            }

            if (isBlank(enrichment.getNostroAccountRef())) {
                addError(errors,
                        "nostroAccountRef",
                        "NOSTRO_ACCOUNT_REQUIRED",
                        "Nostro account is required when cover is required.",
                        ValidationSeverity.BLOCKING);
            }

            if (isBlank(enrichment.getSettlementCurrency())) {
                addError(errors,
                        "settlementCurrency",
                        "SETTLEMENT_CURRENCY_REQUIRED",
                        "Settlement currency is required when cover is required.",
                        ValidationSeverity.BLOCKING);
            }

            if (enrichment.getSettlementAmount() == null) {
                addError(errors,
                        "settlementAmount",
                        "SETTLEMENT_AMOUNT_REQUIRED",
                        "Settlement amount is required when cover is required.",
                        ValidationSeverity.BLOCKING);
            }

            if (enrichment.getSettlementDate() == null) {
                addError(errors,
                        "settlementDate",
                        "SETTLEMENT_DATE_REQUIRED",
                        "Settlement date is required when cover is required.",
                        ValidationSeverity.BLOCKING);
            }
        }
    }

    private RefInterbankChainRequest buildRefRequest(MvtTrOperation operation) {
        TrFinancialAgent creditorAgent = findAgent(operation, FinancialAgentRole.CDTR_AGT);

        return new RefInterbankChainRequest(
                operation.getRefOrdre(),
                operation.getCodeDevise(),
                operation.getMntDevise(),
                operation.getDateValeurTransfert(),
                null,
                creditorAgent == null ? null : creditorAgent.getBicfi(),
                creditorAgent == null ? null : creditorAgent.getCountry(),
                operation.getChargeBearer()
        );
    }

    private InterbankAgentDto toInterbankAgentDto(RefInterbankAgentDto agent) {
        return new InterbankAgentDto(
                agent.agentRole(),
                agent.bicfi(),
                agent.lei(),
                agent.clearingSystemCode(),
                agent.clearingMemberId(),
                agent.agentName(),
                agent.branchId(),
                agent.branchName(),
                agent.country(),
                agent.addressLine1(),
                agent.addressLine2(),
                agent.townName()
        );
    }

    private void validateCorrespondents(
            MvtTrOperation operation,
            List<InterbankValidationErrorDto> errors
    ) {
        TrFinancialAgent creditorAgent = findAgent(operation, FinancialAgentRole.CDTR_AGT);

        if (creditorAgent == null) {
            addError(errors,
                    "creditorAgent",
                    "CREDITOR_AGENT_REQUIRED",
                    "Creditor agent is required.",
                    ValidationSeverity.BLOCKING);
        } else {
            validateBic(errors, "creditorAgent.bicfi", creditorAgent.getBicfi());
        }

        validateAgentIfPresent(operation, FinancialAgentRole.DBTR_AGT, "debtorAgent", errors);
        validateAgentIfPresent(operation, FinancialAgentRole.INTRMY_AGT_1, "intermediaryAgent1", errors);
        validateAgentIfPresent(operation, FinancialAgentRole.INTRMY_AGT_2, "intermediaryAgent2", errors);
        validateAgentIfPresent(operation, FinancialAgentRole.INTRMY_AGT_3, "intermediaryAgent3", errors);
        validateAgentIfPresent(operation, FinancialAgentRole.REIMBURSEMENT_AGT, "reimbursementAgent", errors);
        validateAgentIfPresent(operation, FinancialAgentRole.NOSTRO_AGT, "nostroAgent", errors);

        validateNoDuplicateAdjacentAgents(operation, errors);
    }

    private void validateAgentIfPresent(
            MvtTrOperation operation,
            FinancialAgentRole role,
            String fieldPath,
            List<InterbankValidationErrorDto> errors
    ) {
        TrFinancialAgent agent = findAgent(operation, role);
        if (agent != null) {
            validateBic(errors, fieldPath + ".bicfi", agent.getBicfi());
        }
    }

    private void validateBic(
            List<InterbankValidationErrorDto> errors,
            String fieldPath,
            String bic
    ) {
        if (isBlank(bic)) {
            addError(errors,
                    fieldPath,
                    "BIC_REQUIRED",
                    "BIC is required for the selected bank.",
                    ValidationSeverity.BLOCKING);
            return;
        }

        if (!bic.matches("^[A-Z0-9]{8}([A-Z0-9]{3})?$")) {
            addError(errors,
                    fieldPath,
                    "INVALID_BIC_FORMAT",
                    "BIC must contain 8 or 11 uppercase alphanumeric characters.",
                    ValidationSeverity.BLOCKING);
        }
    }

    private void validateNoDuplicateAdjacentAgents(
            MvtTrOperation operation,
            List<InterbankValidationErrorDto> errors
    ) {
        List<FinancialAgentRole> chain = List.of(
                FinancialAgentRole.DBTR_AGT,
                FinancialAgentRole.INTRMY_AGT_1,
                FinancialAgentRole.INTRMY_AGT_2,
                FinancialAgentRole.INTRMY_AGT_3,
                FinancialAgentRole.CDTR_AGT
        );

        TrFinancialAgent previous = null;

        for (FinancialAgentRole role : chain) {
            TrFinancialAgent current = findAgent(operation, role);

            if (current == null) {
                continue;
            }

            if (previous != null
                    && notBlank(previous.getBicfi())
                    && previous.getBicfi().equals(current.getBicfi())) {
                addError(errors,
                        role.name(),
                        "DUPLICATE_ADJACENT_AGENT",
                        "Two adjacent agents in the payment path cannot have the same BIC.",
                        ValidationSeverity.BLOCKING);
            }

            previous = current;
        }
    }

    private CoverDecision determineCoverDecision(
            MvtTrOperation operation,
            DetermineCoverRequest request
    ) {
        if (Boolean.TRUE.equals(request.forceCover())) {
            return new CoverDecision(
                    PaymentRouteType.COVER_REQUIRED,
                    true,
                    resolveCoverMessageType(request.targetFormat()),
                    "Cover forced by validator."
            );
        }

        boolean hasIntermediary =
                findAgent(operation, FinancialAgentRole.INTRMY_AGT_1) != null
                        || findAgent(operation, FinancialAgentRole.INTRMY_AGT_2) != null
                        || findAgent(operation, FinancialAgentRole.INTRMY_AGT_3) != null;

        boolean hasNostro =
                notBlank(request.nostroAccountRef())
                        || notBlank(request.nostroAgentBic())
                        || findAgent(operation, FinancialAgentRole.NOSTRO_AGT) != null;

        boolean hasReimbursementAgent =
                findAgent(operation, FinancialAgentRole.REIMBURSEMENT_AGT) != null;

        if (hasNostro || hasReimbursementAgent) {
            return new CoverDecision(
                    PaymentRouteType.COVER_REQUIRED,
                    true,
                    resolveCoverMessageType(request.targetFormat()),
                    "Cover required because Nostro or reimbursement agent is defined."
            );
        }

        if (hasIntermediary) {
            return new CoverDecision(
                    PaymentRouteType.WITH_INTERMEDIARY,
                    false,
                    CoverMessageType.NONE,
                    "Payment path uses intermediary agent without separate cover."
            );
        }

        return new CoverDecision(
                PaymentRouteType.DIRECT,
                false,
                CoverMessageType.NONE,
                "Direct payment path. pacs.008 only."
        );
    }

    private CoverMessageType resolveCoverMessageType(String targetFormat) {
        if ("ISO20022".equalsIgnoreCase(targetFormat)) {
            return CoverMessageType.PACS_009_COV;
        }

        return CoverMessageType.MT202_COV;
    }

    private void validatePaymentPathRequest(SavePaymentPathRequest request) {
        if (request.agents() == null || request.agents().isEmpty()) {
            throw new IllegalArgumentException("At least one payment agent is required.");
        }

        boolean hasCreditorAgent = request.agents().stream()
                .anyMatch(agent -> agent.agentRole() == FinancialAgentRole.CDTR_AGT);

        if (!hasCreditorAgent) {
            throw new IllegalArgumentException("Creditor agent CDTR_AGT is required.");
        }
    }

    private void validateInstructions(SaveInterbankInstructionsRequest request) {
        if (request.instructions() == null) {
            throw new IllegalArgumentException("Instruction list is required.");
        }

        for (InterbankInstructionDto instruction : request.instructions()) {
            if (instruction.instructionType() == null) {
                throw new IllegalArgumentException("Instruction type is required.");
            }

            if (instruction.targetFormat() == null) {
                throw new IllegalArgumentException("Instruction target format is required.");
            }

            if (notBlank(instruction.instructionCode())
                    && instruction.instructionCode().length() > 35) {
                throw new IllegalArgumentException("Instruction code must not exceed 35 characters.");
            }

            if (notBlank(instruction.instructionText())
                    && instruction.instructionText().length() > 500) {
                throw new IllegalArgumentException("Instruction text must not exceed 500 characters.");
            }
        }
    }

    private void assertValidatorActionAllowed(MvtTrOperation operation) {
        if (operation == null) {
            throw new IllegalArgumentException("Transfer operation is required.");
        }

        if (operation.getStatus() == TransferOperationStatus.A) {
            throw new IllegalStateException("Applied transfer operation cannot be enriched.");
        }
    }

    private TrInterbankEnrichment getOrCreateEnrichment(MvtTrOperation operation) {
        return enrichmentRepository.findByOperation_RefOperation(operation.getRefOperation())
                .orElseGet(() -> enrichmentRepository.save(
                        TrInterbankEnrichment.builder()
                                .operation(operation)
                                .enrichmentStatus(InterbankEnrichmentStatus.NOT_ENRICHED)
                                .paymentPathStatus(PaymentPathStatus.NOT_DEFINED)
                                .paymentRouteType(PaymentRouteType.DIRECT_NOSTRO)
                                .coverRequired("N")
                                .coverMessageType(CoverMessageType.NONE)
                                .manualOverride("N")
                                .uetr(operation.getUetr())
                                .settlementCurrency(operation.getCodeDevise())
                                .settlementAmount(operation.getMntDevise())
                                .settlementDate(operation.getDateValeurTransfert())
                                .build()
                ));
    }

    private TrInterbankEnrichment findOrBuildDefault(MvtTrOperation operation) {
        return enrichmentRepository.findByOperation_RefOperation(operation.getRefOperation())
                .orElseGet(() -> TrInterbankEnrichment.builder()
                        .operation(operation)
                        .enrichmentStatus(InterbankEnrichmentStatus.NOT_ENRICHED)
                        .paymentPathStatus(PaymentPathStatus.NOT_DEFINED)
                        .coverRequired("N")
                        .coverMessageType(CoverMessageType.NONE)
                        .build());
    }

    private PaymentPathStatus resolvePaymentPathStatus(MvtTrOperation operation) {
        TrFinancialAgent creditorAgent = findAgent(operation, FinancialAgentRole.CDTR_AGT);

        if (creditorAgent == null || isBlank(creditorAgent.getBicfi())) {
            return PaymentPathStatus.INCOMPLETE;
        }

        return PaymentPathStatus.COMPLETE;
    }

    private InterbankEnrichmentStatus resolveEnrichmentStatus(PaymentPathStatus pathStatus) {
        return switch (pathStatus) {
            case COMPLETE -> InterbankEnrichmentStatus.ENRICHED;
            case INCOMPLETE -> InterbankEnrichmentStatus.PARTIALLY_ENRICHED;
            case INVALID -> InterbankEnrichmentStatus.MANUAL_REVIEW_REQUIRED;
            case NOT_DEFINED -> InterbankEnrichmentStatus.NOT_ENRICHED;
        };
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

    private void addFinancialAgentSnapshot(
            MvtTrOperation operation,
            InterbankAgentDto dto
    ) {
        TrFinancialAgent agent = new TrFinancialAgent();

        agent.setAgentRole(dto.agentRole());
        agent.setBicfi(cleanUpper(dto.bicfi()));
        agent.setLei(cleanUpper(dto.lei()));
        agent.setClearingSystemCode(clean(dto.clearingSystemCode()));
        agent.setClearingMemberId(clean(dto.clearingMemberId()));
        agent.setAgentName(clean(dto.agentName()));
        agent.setBranchId(clean(dto.branchId()));
        agent.setBranchName(clean(dto.branchName()));
        agent.setCountry(cleanUpper(dto.country()));
        agent.setAddressLine1(clean(dto.addressLine1()));
        agent.setAddressLine2(clean(dto.addressLine2()));
        agent.setTownName(clean(dto.townName()));

        operation.addFinancialAgent(agent);
    }

    private TrFinancialAgent findAgent(
            MvtTrOperation operation,
            FinancialAgentRole role
    ) {
        if (operation.getFinancialAgents() == null) {
            return null;
        }

        return operation.getFinancialAgents()
                .stream()
                .filter(agent -> agent.getAgentRole() == role)
                .findFirst()
                .orElse(null);
    }

    private InterbankEnrichmentResponse toResponse(
            MvtTrOperation operation,
            TrInterbankEnrichment enrichment,
            List<TrInterbankInstruction> instructions,
            List<InterbankValidationErrorDto> warnings
    ) {
        return new InterbankEnrichmentResponse(
                operation.getRefOrdre(),
                operation.getRefOperation(),

                operation.getCodeDevise(),
                operation.getMntDevise(),
                operation.getDateValeurTransfert(),
                operation.getChargeBearer(),

                enrichment.getEnrichmentStatus(),
                enrichment.getPaymentPathStatus(),
                enrichment.getPaymentRouteType(),

                enrichment.getSettlementMethod(),
                enrichment.getSettlementAccountRef(),
                enrichment.getSettlementCurrency(),
                enrichment.getSettlementAmount(),
                enrichment.getSettlementDate(),

                enrichment.getNostroAccountRef(),
                enrichment.getNostroCurrency(),
                enrichment.getNostroAgentBic(),

                "O".equals(enrichment.getCoverRequired()),
                enrichment.getCoverMessageType(),
                enrichment.getCoverReason(),

                enrichment.getPacs008MessageId(),
                enrichment.getPacs009CovMessageId(),
                enrichment.getUetr(),

                mapAgents(operation),
                instructions.stream().map(this::toInstructionDto).toList(),
                warnings
        );
    }

    private List<InterbankAgentDto> mapAgents(MvtTrOperation operation) {
        if (operation.getFinancialAgents() == null) {
            return List.of();
        }

        return operation.getFinancialAgents()
                .stream()
                .map(agent -> new InterbankAgentDto(
                        agent.getAgentRole(),
                        agent.getBicfi(),
                        agent.getLei(),
                        agent.getClearingSystemCode(),
                        agent.getClearingMemberId(),
                        agent.getAgentName(),
                        agent.getBranchId(),
                        agent.getBranchName(),
                        agent.getCountry(),
                        agent.getAddressLine1(),
                        agent.getAddressLine2(),
                        agent.getTownName()
                ))
                .toList();
    }

    private InterbankInstructionDto toInstructionDto(TrInterbankInstruction instruction) {
        return new InterbankInstructionDto(
                instruction.getInstructionType(),
                instruction.getTargetFormat(),
                instruction.getTargetAgentRole(),
                instruction.getInstructionCode(),
                instruction.getInstructionText()
        );
    }

    private void addError(
            List<InterbankValidationErrorDto> errors,
            String fieldPath,
            String errorCode,
            String errorMessage,
            ValidationSeverity severity
    ) {
        errors.add(new InterbankValidationErrorDto(
                fieldPath,
                errorCode,
                errorMessage,
                severity
        ));
    }

    private String clean(String value) {
        return value == null ? null : value.trim();
    }

    private String cleanUpper(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private record CoverDecision(
            PaymentRouteType paymentRouteType,
            boolean coverRequired,
            CoverMessageType coverMessageType,
            String reason
    ) {
    }
}