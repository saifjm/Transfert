package com.smi.mstr.transfer.application.mapper;

import com.smi.mstr.transfer.domain.entity.*;
import com.smi.mstr.transfer.domain.enums.*;
import com.smi.mstr.transfer.dto.normalized.*;
import org.springframework.stereotype.Component;

@Component
public class TransferOrderDataMapper {

    public TrParty toParty(PartyDto dto, PartyRole role) {
        TrParty party = TrParty.builder()
                .partyRole(role)
                .partyType(dto.partyType())
                .name(dto.name())
                .localPartyId(dto.localPartyId())
                .localIdType(dto.localIdType())
                .customerCode(dto.customerCode())
                .countryOfResidence(dto.countryOfResidence())
                .birthDate(dto.birthDate())
                .cityOfBirth(dto.cityOfBirth())
                .countryOfBirth(dto.countryOfBirth())
                .build();

        if (dto.postalAddresses() != null) {
            dto.postalAddresses().forEach(addressDto ->
                    party.addAddress(toAddress(addressDto))
            );
        }

        if (dto.identifications() != null) {
            dto.identifications().forEach(identDto ->
                    party.addIdentification(toIdentification(identDto))
            );
        }

        return party;
    }

    public TrPartyPostalAddress toAddress(PostalAddressDto dto) {
        return TrPartyPostalAddress.builder()
                .addressType(dto.addressType())
                .streetName(dto.streetName())
                .buildingNumber(dto.buildingNumber())
                .postCode(dto.postCode())
                .townName(dto.townName())
                .countrySubDivision(dto.countrySubDivision())
                .country(dto.country())
                .addressLine1(dto.addressLine1())
                .addressLine2(dto.addressLine2())
                .addressLine3(dto.addressLine3())
                .build();
    }

    public TrPartyIdentification toIdentification(PartyIdentificationDto dto) {
        return TrPartyIdentification.builder()
                .identificationScope(dto.identificationScope())
                .identificationType(dto.identificationType())
                .identificationValue(dto.identificationValue())
                .issuer(dto.issuer())
                .schemeNameCode(dto.schemeNameCode())
                .schemeNameProprietary(dto.schemeNameProprietary())
                .build();
    }

    public TrAccount toAccount(AccountDto dto, AccountRole role, TrParty party) {
        return TrAccount.builder()
                .accountRole(role)
                .party(party)
                .iban(dto.iban())
                .otherAccountId(dto.otherAccountId())
                .accountScheme(dto.accountScheme())
                .accountCurrency(dto.accountCurrency())
                .accountName(dto.accountName())
                .coreAccountId(dto.coreAccountId())
                .ribLocal(dto.ribLocal())
                .build();
    }

    public TrFinancialAgent toFinancialAgent(
            FinancialAgentDto dto,
            FinancialAgentRole role
    ) {
        return TrFinancialAgent.builder()
                .agentRole(role)
                .bicfi(dto.bicfi())
                .lei(dto.lei())
                .clearingSystemCode(dto.clearingSystemCode())
                .clearingMemberId(dto.clearingMemberId())
                .agentName(dto.agentName())
                .branchId(dto.branchId())
                .branchName(dto.branchName())
                .country(dto.country())
                .addressLine1(dto.addressLine1())
                .addressLine2(dto.addressLine2())
                .townName(dto.townName())
                .build();
    }
}