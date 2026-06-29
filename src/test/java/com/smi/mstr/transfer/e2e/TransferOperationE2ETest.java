package com.smi.mstr.transfer.e2e;

import com.smi.mstr.transfer.dto.CreateTransferOrderRequest;
import com.smi.mstr.transfer.dto.SaveTransferDraftRequest;
import com.smi.mstr.transfer.dto.normalized.AccountDto;
import com.smi.mstr.transfer.dto.normalized.FinancialAgentDto;
import com.smi.mstr.transfer.dto.normalized.PartyDto;
import com.smi.mstr.transfer.dto.normalized.PartyIdentificationDto;
import com.smi.mstr.transfer.dto.normalized.PostalAddressDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class TransferOperationE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    void createManualOrder_shouldCreateOperationThroughRestApi() throws Exception {
        CreateTransferOrderRequest request = createRequest();

        String responseBody = mockMvc.perform(post("/api/ms-tr/operations")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operationRef").exists())
                .andExpect(jsonPath("$.status").value("X"))
                .andExpect(jsonPath("$.completionStatus").value("COMPLETE"))
                .andExpect(jsonPath("$.debtor.name").value("SOCIETE IMPORTATRICE TUNISIENNE"))
                .andExpect(jsonPath("$.creditor.name").value("FOREIGN SUPPLIER LTD"))
                .andExpect(jsonPath("$.creditorAgent.bicfi").value("AGRIFRPPXXX"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(responseBody);
        String operationRef = json.get("operationRef").asString();

        assertThat(operationRepository.findByOperationRef(operationRef)).isPresent();
        assertThat(eventRepository.findAll()).hasSize(1);
    }

    @Test
    void createThenSaveDraft_shouldUpdateOperationThroughRestApi() throws Exception {
        CreateTransferOrderRequest createRequest = createRequest();

        String createResponse = mockMvc.perform(post("/api/ms-tr/operations")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String operationRef = objectMapper.readTree(createResponse)
                .get("operationRef")
                .asString();

        SaveTransferDraftRequest draftRequest = saveDraftRequest();

        mockMvc.perform(put("/api/ms-tr/operations/{operationRef}/draft", operationRef)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(draftRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operationRef").value(operationRef))
                .andExpect(jsonPath("$.status").value("X"))
                .andExpect(jsonPath("$.completionStatus").value("COMPLETE"))
                .andExpect(jsonPath("$.orderCurrency").value("USD"))
                .andExpect(jsonPath("$.transferCurrency").value("USD"))
                .andExpect(jsonPath("$.creditor.name").value("UPDATED BENEFICIARY LTD"))
                .andExpect(jsonPath("$.creditorAgent.bicfi").value("BNPAFRPPXXX"));

        assertThat(eventRepository.findAll()).hasSize(2);
    }

    @Test
    void createThenConsultInProgress_shouldReturnOperationInList() throws Exception {
        CreateTransferOrderRequest createRequest = createRequest();

        String createResponse = mockMvc.perform(post("/api/ms-tr/operations")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String operationRef = objectMapper.readTree(createResponse)
                .get("operationRef")
                .asString();

        mockMvc.perform(get("/api/ms-tr/operations/in-progress")
                        .param("branchCode", "AG001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].operationRef").value(operationRef))
                .andExpect(jsonPath("$[0].status").value("X"))
                .andExpect(jsonPath("$[0].branchCode").value("AG001"));
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