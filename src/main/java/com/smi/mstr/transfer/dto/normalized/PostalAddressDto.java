package com.smi.mstr.transfer.dto.normalized;

public record PostalAddressDto(
        String addressType,
        String streetName,
        String buildingNumber,
        String postCode,
        String townName,
        String countrySubDivision,
        String country,
        String addressLine1,
        String addressLine2,
        String addressLine3
) {}