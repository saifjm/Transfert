package com.smi.mstr.transfer.application;

import com.smi.mstr.transfer.application.mapper.TransferOperationResponseMapper;
import com.smi.mstr.transfer.application.mapper.TransferOrderDataMapper;
import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.domain.entity.TrOperationEvent;
import com.smi.mstr.transfer.domain.enums.*;
import com.smi.mstr.transfer.domain.repository.MvtTrOperationRepository;
import com.smi.mstr.transfer.domain.repository.TrOperationEventRepository;
import com.smi.mstr.transfer.dto.CreateTransferOrderRequest;
import com.smi.mstr.transfer.dto.SaveTransferDraftRequest;
import com.smi.mstr.transfer.dto.TransferOperationResponse;
import com.smi.mstr.transfer.dto.normalized.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class TransferOperationServiceTest {

    @Mock
    private MvtTrOperationRepository operationRepository;

    @Mock
    private TrOperationEventRepository eventRepository;

    @Mock
    private TransferReferenceService referenceService;

    private TransferOrderDataMapper orderDataMapper;
    private TransferOperationResponseMapper responseMapper;
    private TransferOperationService service;

    private final AtomicLong technicalIdSequence = new AtomicLong(100L);

    @BeforeEach
    void setUp() {
        orderDataMapper = new TransferOrderDataMapper();
        responseMapper = new TransferOperationResponseMapper();

        service = new TransferOperationService(
                operationRepository,
                eventRepository,
                referenceService,
                orderDataMapper,
                responseMapper
        );

        when(operationRepository.save(any(MvtTrOperation.class)))
                .thenAnswer(invocation -> {
                    MvtTrOperation op = invocation.getArgument(0);
                    if (op.getRefOperation() == null) {
                        op.setRefOperation(technicalIdSequence.incrementAndGet());
                    }
                    return op;
                });

        when(eventRepository.save(any(TrOperationEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createManualOrder_shouldCreateOperationWithStatusXAndNormalizedData() {
        CreateTransferOrderRequest request = completeCreateRequest();

        when(referenceService.generateReference()).thenReturn("TR-2026-000001");
        doNothing().when(referenceService).ensureUnique("TR-2026-000001");

        TransferOperationResponse response = service.createManualOrder(request);

        assertThat(response).isNotNull();
        assertThat(response.operationRef()).isEqualTo("TR-2026-000001");
        assertThat(response.status()).isEqualTo(TransferOperationStatus.X);
        assertThat(response.completionStatus()).isEqualTo(CompletionStatus.COMPLETE);
        assertThat(response.transferType()).isEqualTo(TransferType.C);
        assertThat(response.swiftPriority()).isEqualTo(SwiftPriority.N);
        assertThat(response.branchCode()).isEqualTo("AG001");

        assertThat(response.debtor()).isNotNull();
        assertThat(response.debtor().name()).isEqualTo("SOCIETE IMPORTATRICE TUNISIENNE");
        assertThat(response.debtorAccount()).isNotNull();
        assertThat(response.debtorAccount().coreAccountId()).isEqualTo("001001000123456");

        assertThat(response.creditor()).isNotNull();
        assertThat(response.creditor().name()).isEqualTo("FOREIGN SUPPLIER LTD");
        assertThat(response.creditorAccount()).isNotNull();
        assertThat(response.creditorAccount().iban()).isEqualTo("FR7630006000011234567890189");

        assertThat(response.creditorAgent()).isNotNull();
        assertThat(response.creditorAgent().bicfi()).isEqualTo("AGRIFRPPXXX");

        verify(referenceService).generateReference();
        verify(referenceService).ensureUnique("TR-2026-000001");
        verify(operationRepository).save(any(MvtTrOperation.class));
        verify(eventRepository).save(any(TrOperationEvent.class));
    }

    @Test
    void createManualOrder_shouldRegisterOperationCreatedEvent() {
        CreateTransferOrderRequest request = completeCreateRequest();

        when(referenceService.generateReference()).thenReturn("TR-2026-000002");
        doNothing().when(referenceService).ensureUnique("TR-2026-000002");

        service.createManualOrder(request);

        ArgumentCaptor<TrOperationEvent> eventCaptor =
                ArgumentCaptor.forClass(TrOperationEvent.class);

        verify(eventRepository).save(eventCaptor.capture());

        TrOperationEvent event = eventCaptor.getValue();

        assertThat(event.getEventType()).isEqualTo(OperationEventType.OPERATION_CREATED);
        assertThat(event.getOldStatus()).isNull();
        assertThat(event.getNewStatus()).isEqualTo(TransferOperationStatus.X);
        assertThat(event.getActorUserId()).isEqualTo("agent01");
        assertThat(event.getActorRole()).isEqualTo("AGENT_SAISIE");
        assertThat(event.getCommentText()).isEqualTo("Manual normalized transfer order created");
        assertThat(event.getOperation()).isNotNull();
        assertThat(event.getOperation().getOperationRef()).isEqualTo("TR-2026-000002");
    }

    @Test
    void createManualOrder_shouldSetCompletionStatusPartialWhenCreditorAgentIdentifierIsMissing() {
        CreateTransferOrderRequest request = createRequestWithoutCreditorAgentIdentifier();

        when(referenceService.generateReference()).thenReturn("TR-2026-000003");
        doNothing().when(referenceService).ensureUnique("TR-2026-000003");

        TransferOperationResponse response = service.createManualOrder(request);

        assertThat(response.completionStatus()).isEqualTo(CompletionStatus.PARTIAL);
        assertThat(response.creditorAgent()).isNotNull();
        assertThat(response.creditorAgent().bicfi()).isNull();
        assertThat(response.creditorAgent().clearingMemberId()).isNull();
        assertThat(response.creditorAgent().agentName()).isNull();
    }

    @Test
    void saveDraft_shouldUpdateHeaderReplaceNormalizedDataAndRegisterEvent() {
        MvtTrOperation existing = existingEditableOperation();
        SaveTransferDraftRequest request = completeSaveDraftRequest();

        when(operationRepository.findByOperationRef("TR-2026-000010"))
                .thenReturn(Optional.of(existing));

        TransferOperationResponse response = service.saveDraft("TR-2026-000010", request);

        assertThat(response.operationRef()).isEqualTo("TR-2026-000010");
        assertThat(response.status()).isEqualTo(TransferOperationStatus.X);
        assertThat(response.completionStatus()).isEqualTo(CompletionStatus.COMPLETE);

        assertThat(response.orderAmount()).isEqualByComparingTo("2500.000");
        assertThat(response.orderCurrency()).isEqualTo("USD");
        assertThat(response.transferAmount()).isEqualByComparingTo("2500.000");
        assertThat(response.transferCurrency()).isEqualTo("USD");

        assertThat(response.creditor().name()).isEqualTo("UPDATED BENEFICIARY LTD");
        assertThat(response.creditorAgent().bicfi()).isEqualTo("BNPAFRPPXXX");

        ArgumentCaptor<TrOperationEvent> eventCaptor =
                ArgumentCaptor.forClass(TrOperationEvent.class);

        verify(eventRepository).save(eventCaptor.capture());

        TrOperationEvent event = eventCaptor.getValue();

        assertThat(event.getEventType()).isEqualTo(OperationEventType.DRAFT_SAVED);
        assertThat(event.getOldStatus()).isEqualTo(TransferOperationStatus.X);
        assertThat(event.getNewStatus()).isEqualTo(TransferOperationStatus.X);
        assertThat(event.getActorUserId()).isEqualTo("agent02");
        assertThat(event.getActorRole()).isEqualTo("AGENT_SAISIE");
        assertThat(event.getCommentText()).isEqualTo("Draft updated");
    }

    @Test
    void saveDraft_shouldThrowWhenOperationNotFound() {
        when(operationRepository.findByOperationRef("UNKNOWN"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.saveDraft("UNKNOWN", completeSaveDraftRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transfer operation not found");

        verify(operationRepository, never()).save(any());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void saveDraft_shouldThrowWhenOperationIsNotEditable() {
        MvtTrOperation validatedOperation = existingEditableOperation();
        validatedOperation.setStatus(TransferOperationStatus.V);

        when(operationRepository.findByOperationRef("TR-2026-000011"))
                .thenReturn(Optional.of(validatedOperation));

        assertThatThrownBy(() -> service.saveDraft("TR-2026-000011", completeSaveDraftRequest()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only operations with status X");

        verify(operationRepository, never()).save(any());
        verify(eventRepository, never()).save(any());
    }

    private MvtTrOperation existingEditableOperation() {
        MvtTrOperation operation = MvtTrOperation.builder()
                .refOperation(10L)
                .operationRef("TR-2026-000010")
                .dateOperation(LocalDate.now())
                .status(TransferOperationStatus.X)
                .completionStatus(CompletionStatus.PARTIAL)
                .transferType(TransferType.C)
                .swiftPriority(SwiftPriority.N)
                .branchCode("AG001")
                .createdBy("agent01")
                .sourceChannel(OriginChannel.AGENCY)
                .build();

        return operation;
    }

    private CreateTransferOrderRequest completeCreateRequest() {
        return new CreateTransferOrderRequest(
                TransferType.C,
                SwiftPriority.N,
                "DOS-2026-00001",
                LocalDate.of(2026, 6, 8),
                "AG001",
                "agent01",

                new BigDecimal("1000.000"),
                "EUR",
                new BigDecimal("1000.000"),
                "EUR",

                "GDDS",
                "Import goods payment",
                "Invoice INV-2026-001",
                "SHAR",

                debtorParty(),
                debtorAccount(),

                creditorParty("FOREIGN SUPPLIER LTD"),
                creditorAccount(),

                creditorAgent("AGRIFRPPXXX", "CREDIT AGRICOLE")
        );
    }

    private CreateTransferOrderRequest createRequestWithoutCreditorAgentIdentifier() {
        return new CreateTransferOrderRequest(
                TransferType.C,
                SwiftPriority.N,
                "DOS-2026-00002",
                LocalDate.of(2026, 6, 8),
                "AG001",
                "agent01",

                new BigDecimal("1000.000"),
                "EUR",
                new BigDecimal("1000.000"),
                "EUR",

                "GDDS",
                "Import goods payment",
                "Invoice INV-2026-002",
                "SHAR",

                debtorParty(),
                debtorAccount(),

                creditorParty("FOREIGN SUPPLIER LTD"),
                creditorAccount(),

                creditorAgent(null, null)
        );
    }

    private SaveTransferDraftRequest completeSaveDraftRequest() {
        return new SaveTransferDraftRequest(
                "agent02",
                "Draft updated",

                new BigDecimal("2500.000"),
                "USD",
                new BigDecimal("2500.000"),
                "USD",

                "GDDS",
                "Updated import goods payment",
                "Updated invoice INV-2026-999",
                "SHAR",

                debtorParty(),
                debtorAccount(),

                creditorParty("UPDATED BENEFICIARY LTD"),
                creditorAccount(),

                creditorAgent("BNPAFRPPXXX", "BNP PARIBAS")
        );
    }

    private PartyDto debtorParty() {
        return new PartyDto(
                PartyType.ORG,
                "SOCIETE IMPORTATRICE TUNISIENNE",
                "1234567A",
                "TXID",
                "CUST001",
                "TN",
                null,
                null,
                null,
                List.of(new PostalAddressDto(
                        null,
                        null,
                        null,
                        null,
                        "Tunis",
                        null,
                        "TN",
                        "Rue de Tunis",
                        null,
                        null
                )),
                List.of(new PartyIdentificationDto(
                        "ORG_ID",
                        "TXID",
                        "1234567A",
                        "TN",
                        null,
                        null
                ))
        );
    }

    private PartyDto creditorParty(String name) {
        return new PartyDto(
                PartyType.ORG,
                name,
                null,
                null,
                null,
                "FR",
                null,
                null,
                null,
                List.of(new PostalAddressDto(
                        null,
                        null,
                        null,
                        null,
                        "Paris",
                        null,
                        "FR",
                        "10 Rue Exemple",
                        null,
                        null
                )),
                List.of(new PartyIdentificationDto(
                        "ORG_ID",
                        "CUST",
                        "SUPPLIER-001",
                        null,
                        null,
                        null
                ))
        );
    }

    private AccountDto debtorAccount() {
        return new AccountDto(
                "TN5900000000000000000000",
                null,
                null,
                "TND",
                "SOCIETE IMPORTATRICE TUNISIENNE",
                "001001000123456",
                null
        );
    }

    private AccountDto creditorAccount() {
        return new AccountDto(
                "FR7630006000011234567890189",
                null,
                null,
                "EUR",
                "FOREIGN SUPPLIER LTD",
                null,
                null
        );
    }

    private FinancialAgentDto creditorAgent(String bicfi, String agentName) {
        return new FinancialAgentDto(
                bicfi,
                null,
                null,
                null,
                agentName,
                null,
                null,
                "FR",
                null,
                null,
                "Paris"
        );
    }
}