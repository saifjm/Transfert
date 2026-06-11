package com.smi.mstr.transfer.application.mapper;

import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.domain.enums.*;
import com.smi.mstr.transfer.dto.TransferOperationResponse;
import com.smi.mstr.transfer.dto.normalized.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TransferOperationResponseMapperTest {

    private final TransferOrderDataMapper orderDataMapper = new TransferOrderDataMapper();
    private final TransferOperationResponseMapper responseMapper = new TransferOperationResponseMapper();

    @Test
    void toResponse_shouldMapNormalizedDebtorCreditorAccountsAndCreditorAgent() {
        MvtTrOperation operation = MvtTrOperation.builder()
                .refOperation(1L)
                .operationRef("TR-2026-000001")
                .status(TransferOperationStatus.X)
                .completionStatus(CompletionStatus.COMPLETE)
                .transferType(TransferType.C)
                .swiftPriority(SwiftPriority.N)
                .branchCode("AG001")
                .numDossier("DOS-001")
                .dateOperation(LocalDate.now())
                .dateDossier(LocalDate.now())
                .orderAmount(new BigDecimal("1000.000"))
                .orderCurrency("EUR")
                .transferAmount(new BigDecimal("1000.000"))
                .transferCurrency("EUR")
                .purposeCode("GDDS")
                .purposeProprietary("Import goods payment")
                .remittanceUnstructured("Invoice INV-001")
                .chargeBearer("SHAR")
                .createdAt(LocalDateTime.now())
                .version(0L)
                .build();

        var debtor = orderDataMapper.toParty(party("DEBTOR SA", "TN"), PartyRole.DBTR);
        var creditor = orderDataMapper.toParty(party("CREDITOR LTD", "FR"), PartyRole.CDTR);

        operation.addParty(debtor);
        operation.addParty(creditor);

        operation.addAccount(orderDataMapper.toAccount(
                account("TN5900000000000000000000", "TND"),
                AccountRole.DBTR_ACCT,
                debtor
        ));

        operation.addAccount(orderDataMapper.toAccount(
                account("FR7630006000011234567890189", "EUR"),
                AccountRole.CDTR_ACCT,
                creditor
        ));

        operation.addFinancialAgent(orderDataMapper.toFinancialAgent(
                new FinancialAgentDto(
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
                ),
                FinancialAgentRole.CDTR_AGT
        ));

        TransferOperationResponse response = responseMapper.toResponse(operation);

        assertThat(response.operationRef()).isEqualTo("TR-2026-000001");
        assertThat(response.debtor().name()).isEqualTo("DEBTOR SA");
        assertThat(response.creditor().name()).isEqualTo("CREDITOR LTD");
        assertThat(response.debtorAccount().iban()).isEqualTo("TN5900000000000000000000");
        assertThat(response.creditorAccount().iban()).isEqualTo("FR7630006000011234567890189");
        assertThat(response.creditorAgent().bicfi()).isEqualTo("AGRIFRPPXXX");
    }

    @Test
    void toResponse_shouldReturnNullBlocksWhenNormalizedDataIsMissing() {
        MvtTrOperation operation = MvtTrOperation.builder()
                .refOperation(1L)
                .operationRef("TR-2026-EMPTY")
                .status(TransferOperationStatus.X)
                .completionStatus(CompletionStatus.EMPTY)
                .transferType(TransferType.C)
                .dateOperation(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .version(0L)
                .build();

        TransferOperationResponse response = responseMapper.toResponse(operation);

        assertThat(response.debtor()).isNull();
        assertThat(response.debtorAccount()).isNull();
        assertThat(response.creditor()).isNull();
        assertThat(response.creditorAccount()).isNull();
        assertThat(response.creditorAgent()).isNull();
    }

    private PartyDto party(String name, String country) {
        return new PartyDto(
                PartyType.ORG,
                name,
                null,
                null,
                null,
                country,
                null,
                null,
                null,
                List.of(),
                List.of()
        );
    }

    private AccountDto account(String iban, String currency) {
        return new AccountDto(
                iban,
                null,
                null,
                currency,
                null,
                null,
                null
        );
    }
}
