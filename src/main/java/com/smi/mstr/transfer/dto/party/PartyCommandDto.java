package com.smi.mstr.transfer.dto.party;

import com.smi.mstr.transfer.domain.enums.PartyRole;
import com.smi.mstr.transfer.domain.enums.PartyType;
import com.smi.mstr.transfer.domain.enums.ResidencyStatus;

public record PartyCommandDto(
        PartyRole partyRole,
        PartyType partyType,
        Integer sequenceNo,

        Long customerId,
        String externalPartyRef,
        String name,
        String countryCode,
        ResidencyStatus residencyStatus,

        String identificationType,
        String identificationValue,
        String identificationIssuer,
        String identificationScheme,
        String lei,

        String addressLine1,
        String addressLine2,
        String addressLine3,
        String townName,
        String postCode,
        String countrySubDivision,

        String accountNumber,
        String accountIban,
        String accountScheme,
        String accountCurrency,
        String accountType,
        String accountName,

        String bic,
        String bankCode,
        String bankName,
        String bankBranchCode,
        String bankBranchName,
        String bankCountryCode,
        String clearingSystemCode,
        String clearingMemberId,

        Integer agentPosition,
        String routingRole,

        String sourceSystem,
        String sourceReference,
        String partySnapshotJson
) {
}