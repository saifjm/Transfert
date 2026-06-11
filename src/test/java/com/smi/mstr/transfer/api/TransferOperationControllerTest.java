package com.smi.mstr.transfer.api;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import tools.jackson.databind.ObjectMapper;
import com.smi.mstr.transfer.application.TransferOperationQueryService;
import com.smi.mstr.transfer.application.TransferOperationService;
import com.smi.mstr.transfer.domain.enums.*;
import com.smi.mstr.transfer.dto.CreateTransferOrderRequest;
import com.smi.mstr.transfer.dto.TransferOperationResponse;
import com.smi.mstr.transfer.dto.normalized.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransferOperationController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransferOperationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransferOperationService operationService;

    @MockitoBean
    private TransferOperationQueryService queryService;

    @Test
    void createManualOrder_shouldReturnCreatedOperation() throws Exception {
        CreateTransferOrderRequest request = createRequest();

        TransferOperationResponse response = new TransferOperationResponse(
                1L,
                "TR-2026-000001",
                TransferOperationStatus.X,
                CompletionStatus.COMPLETE,
                TransferType.C,
                SwiftPriority.N,
                "AG001",
                "DOS-2026-00001",
                LocalDate.now(),
                LocalDate.now(),
                new BigDecimal("1000.000"),
                "EUR",
                new BigDecimal("1000.000"),
                "EUR",
                "GDDS",
                "Import goods payment",
                "Invoice INV-2026-001",
                "SHAR",
                request.debtor(),
                request.debtorAccount(),
                request.creditor(),
                request.creditorAccount(),
                request.creditorAgent(),
                LocalDateTime.now(),
                null,
                0L
        );

        when(operationService.createManualOrder(any(CreateTransferOrderRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/ms-tr/operations")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operationRef").value("TR-2026-000001"))
                .andExpect(jsonPath("$.status").value("X"))
                .andExpect(jsonPath("$.completionStatus").value("COMPLETE"))
                .andExpect(jsonPath("$.debtor.name").value("SOCIETE IMPORTATRICE TUNISIENNE"))
                .andExpect(jsonPath("$.creditor.name").value("FOREIGN SUPPLIER LTD"))
                .andExpect(jsonPath("$.creditorAgent.bicfi").value("AGRIFRPPXXX"));
    }

    private CreateTransferOrderRequest createRequest() {
        PartyDto debtor = new PartyDto(
                PartyType.ORG,
                "SOCIETE IMPORTATRICE TUNISIENNE",
                "1234567A",
                "TXID",
                "CUST001",
                "TN",
                null,
                null,
                null,
                List.of(),
                List.of()
        );

        AccountDto debtorAccount = new AccountDto(
                "TN5900000000000000000000",
                null,
                null,
                "TND",
                "SOCIETE IMPORTATRICE TUNISIENNE",
                "001001000123456",
                null
        );

        PartyDto creditor = new PartyDto(
                PartyType.ORG,
                "FOREIGN SUPPLIER LTD",
                null,
                null,
                null,
                "FR",
                null,
                null,
                null,
                List.of(),
                List.of()
        );

        AccountDto creditorAccount = new AccountDto(
                "FR7630006000011234567890189",
                null,
                null,
                "EUR",
                "FOREIGN SUPPLIER LTD",
                null,
                null
        );

        FinancialAgentDto creditorAgent = new FinancialAgentDto(
                "AGRIFRPPXXX",
                null,
                null,
                null,
                "CREDIT AGRICOLE",
                null,
                null,
                "FR",
                null,
                null,
                "Paris"
        );

        return new CreateTransferOrderRequest(
                TransferType.C,
                SwiftPriority.N,
                "DOS-2026-00001",
                LocalDate.now(),
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
                debtor,
                debtorAccount,
                creditor,
                creditorAccount,
                creditorAgent
        );
    }
}