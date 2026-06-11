package com.smi.mstr.transfer.dto.normalized;

import com.smi.mstr.transfer.domain.enums.PartyType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record PartyDto(
        @NotNull PartyType partyType,
        @NotBlank String name,

        String localPartyId,
        String localIdType,
        String customerCode,
        String countryOfResidence,

        LocalDate birthDate,
        String cityOfBirth,
        String countryOfBirth,

        @Valid List<PostalAddressDto> postalAddresses,
        @Valid List<PartyIdentificationDto> identifications
) {}