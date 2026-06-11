package com.smi.mstr.transfer.application.mapper;

import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.domain.entity.TrAccount;
import com.smi.mstr.transfer.domain.entity.TrFinancialAgent;
import com.smi.mstr.transfer.domain.entity.TrParty;
import com.smi.mstr.transfer.domain.entity.TrPartyIdentification;
import com.smi.mstr.transfer.domain.entity.TrPartyPostalAddress;
import com.smi.mstr.transfer.domain.enums.AccountRole;
import com.smi.mstr.transfer.domain.enums.FinancialAgentRole;
import com.smi.mstr.transfer.domain.enums.PartyRole;
import com.smi.mstr.transfer.dto.TransferOperationResponse;
import com.smi.mstr.transfer.dto.normalized.AccountDto;
import com.smi.mstr.transfer.dto.normalized.FinancialAgentDto;
import com.smi.mstr.transfer.dto.normalized.PartyDto;
import com.smi.mstr.transfer.dto.normalized.PartyIdentificationDto;
import com.smi.mstr.transfer.dto.normalized.PostalAddressDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TransferOperationResponseMapper {

    public TransferOperationResponse toResponse(MvtTrOperation operation) {
        TrParty debtor = findParty(operation, PartyRole.DBTR);
        TrParty creditor = findParty(operation, PartyRole.CDTR);

        TrAccount debtorAccount = findAccount(operation, AccountRole.DBTR_ACCT);
        TrAccount creditorAccount = findAccount(operation, AccountRole.CDTR_ACCT);

        TrFinancialAgent creditorAgent = findFinancialAgent(operation, FinancialAgentRole.CDTR_AGT);

        return new TransferOperationResponse(
                operation.getRefOperation(),
                operation.getOperationRef(),
                operation.getStatus(),
                operation.getCompletionStatus(),
                operation.getTransferType(),
                operation.getSwiftPriority(),

                operation.getBranchCode(),
                operation.getNumDossier(),
                operation.getDateOperation(),
                operation.getDateDossier(),

                operation.getOrderAmount(),
                operation.getOrderCurrency(),
                operation.getTransferAmount(),
                operation.getTransferCurrency(),

                operation.getPurposeCode(),
                operation.getPurposeProprietary(),
                operation.getRemittanceUnstructured(),
                operation.getChargeBearer(),

                debtor != null ? toPartyDto(debtor) : null,
                debtorAccount != null ? toAccountDto(debtorAccount) : null,

                creditor != null ? toPartyDto(creditor) : null,
                creditorAccount != null ? toAccountDto(creditorAccount) : null,

                creditorAgent != null ? toFinancialAgentDto(creditorAgent) : null,

                operation.getCreatedAt(),
                operation.getUpdatedAt(),
                operation.getVersion()
        );
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

    private TrFinancialAgent findFinancialAgent(MvtTrOperation operation, FinancialAgentRole role) {
        if (operation.getFinancialAgents() == null) {
            return null;
        }

        return operation.getFinancialAgents()
                .stream()
                .filter(agent -> agent.getAgentRole() == role)
                .findFirst()
                .orElse(null);
    }

    private PartyDto toPartyDto(TrParty party) {
        return new PartyDto(
                party.getPartyType(),
                party.getName(),
                party.getLocalPartyId(),
                party.getLocalIdType(),
                party.getCustomerCode(),
                party.getCountryOfResidence(),
                party.getBirthDate(),
                party.getCityOfBirth(),
                party.getCountryOfBirth(),
                party.getPostalAddresses() == null
                        ? List.of()
                        : party.getPostalAddresses()
                          .stream()
                          .map(this::toPostalAddressDto)
                          .toList(),
                party.getIdentifications() == null
                        ? List.of()
                        : party.getIdentifications()
                          .stream()
                          .map(this::toPartyIdentificationDto)
                          .toList()
        );
    }

    private PostalAddressDto toPostalAddressDto(TrPartyPostalAddress address) {
        return new PostalAddressDto(
                address.getAddressType(),
                address.getStreetName(),
                address.getBuildingNumber(),
                address.getPostCode(),
                address.getTownName(),
                address.getCountrySubDivision(),
                address.getCountry(),
                address.getAddressLine1(),
                address.getAddressLine2(),
                address.getAddressLine3()
        );
    }

    private PartyIdentificationDto toPartyIdentificationDto(TrPartyIdentification identification) {
        return new PartyIdentificationDto(
                identification.getIdentificationScope(),
                identification.getIdentificationType(),
                identification.getIdentificationValue(),
                identification.getIssuer(),
                identification.getSchemeNameCode(),
                identification.getSchemeNameProprietary()
        );
    }

    private AccountDto toAccountDto(TrAccount account) {
        return new AccountDto(
                account.getIban(),
                account.getOtherAccountId(),
                account.getAccountScheme(),
                account.getAccountCurrency(),
                account.getAccountName(),
                account.getCoreAccountId(),
                account.getRibLocal()
        );
    }

    private FinancialAgentDto toFinancialAgentDto(TrFinancialAgent agent) {
        return new FinancialAgentDto(
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
        );
    }
}
