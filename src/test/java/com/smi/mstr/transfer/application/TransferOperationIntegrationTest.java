package com.smi.mstr.transfer.application;

import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.dto.CreateTransferOrderRequest;
import com.smi.mstr.transfer.dto.SaveTransferDraftRequest;
import com.smi.mstr.transfer.dto.TransferOperationResponse;
import com.smi.mstr.transfer.dto.normalized.AccountDto;
import com.smi.mstr.transfer.dto.normalized.FinancialAgentDto;
import com.smi.mstr.transfer.dto.normalized.PartyDto;
import com.smi.mstr.transfer.dto.normalized.PartyIdentificationDto;
import com.smi.mstr.transfer.dto.normalized.PostalAddressDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TransferOperationIntegrationTest {

    @Autowired
    private TransferOperationService transferOperationService;

    @Autowired
    private MvtTrOperationRepository operationRepository;

    @Autowired
    private TrOperationEventRepository eventRepository;

    @AfterEach
    void cleanDatabase() {
        eventRepository.deleteAll();
        operationRepository.deleteAll();
    }

    @Test
    void createManualOrder_shouldPersistOperationWithNormalizedDataAndEvent() {
        CreateTransferOrderRequest request = createRequest();

        TransferOperationResponse response = transferOperationService.createManualOrder(request);

        assertThat(response).isNotNull();
        assertThat(response.refOperation()).isNotNull();
        assertThat(response.operationRef()).startsWith("TR-");
        assertThat(response.status()).isEqualTo(TransferOperationStatus.X);
        assertThat(response.completionStatus()).isEqualTo(CompletionStatus.COMPLETE);

        assertThat(response.debtor()).isNotNull();
        assertThat(response.debtor().name()).isEqualTo("SOCIETE IMPORTATRICE TUNISIENNE");

        assertThat(response.creditor()).isNotNull();
        assertThat(response.creditor().name()).isEqualTo("FOREIGN SUPPLIER LTD");

        assertThat(response.creditorAgent()).isNotNull();
        assertThat(response.creditorAgent().bicfi()).isEqualTo("AGRIFRPPXXX");

        MvtTrOperation persisted = operationRepository.findByOperationRef(response.operationRef())
                .orElseThrow();

        assertThat(persisted.getStatus()).isEqualTo(TransferOperationStatus.X);
        assertThat(persisted.getCompletionStatus()).isEqualTo(CompletionStatus.COMPLETE);
        assertThat(persisted.getParties()).hasSize(2);
        assertThat(persisted.getAccounts()).hasSize(2);
        assertThat(persisted.getFinancialAgents()).hasSize(1);

        assertThat(eventRepository.findAll()).hasSize(1);
        assertThat(eventRepository.findAll().get(0).getEventType().name())
                .isEqualTo("OPERATION_CREATED");
    }

    @Test
    void saveDraft_shouldReplaceNormalizedDataAndRegisterEvent() {
        TransferOperationResponse created =
                transferOperationService.createManualOrder(createRequest());

        SaveTransferDraftRequest draftRequest = saveDraftRequest();

        TransferOperationResponse updated =
                transferOperationService.saveDraft(created.operationRef(), draftRequest);

        assertThat(updated.operationRef()).isEqualTo(created.operationRef());
        assertThat(updated.status()).isEqualTo(TransferOperationStatus.X);
        assertThat(updated.completionStatus()).isEqualTo(CompletionStatus.COMPLETE);

        assertThat(updated.orderAmount()).isEqualByComparingTo("2500.000");
        assertThat(updated.orderCurrency()).isEqualTo("USD");
        assertThat(updated.transferAmount()).isEqualByComparingTo("2500.000");
        assertThat(updated.transferCurrency()).isEqualTo("USD");

        assertThat(updated.creditor().name()).isEqualTo("UPDATED BENEFICIARY LTD");
        assertThat(updated.creditorAgent().bicfi()).isEqualTo("BNPAFRPPXXX");

        MvtTrOperation persisted = operationRepository.findByOperationRef(created.operationRef())
                .orElseThrow();

        assertThat(persisted.getParties()).hasSize(2);
        assertThat(persisted.getAccounts()).hasSize(2);
        assertThat(persisted.getFinancialAgents()).hasSize(1);

        assertThat(eventRepository.findAll()).hasSize(2);
    }

    private CreateTransferOrderRequest createRequest() {
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

    private SaveTransferDraftRequest saveDraftRequest() {
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