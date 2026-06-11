package com.smi.mstr.transfer.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TR_PARTY_POSTAL_ADDRESS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrPartyPostalAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tr_party_address_seq")
    @SequenceGenerator(
            name = "tr_party_address_seq",
            sequenceName = "SEQ_TR_PARTY_ADDRESS",
            allocationSize = 1
    )
    @Column(name = "ADDRESS_ID")
    private Long addressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARTY_ID", nullable = false)
    private TrParty party;

    @Column(name = "ADDRESS_TYPE", length = 10)
    private String addressType;

    @Column(name = "STREET_NAME", length = 70)
    private String streetName;

    @Column(name = "BUILDING_NUMBER", length = 16)
    private String buildingNumber;

    @Column(name = "POST_CODE", length = 16)
    private String postCode;

    @Column(name = "TOWN_NAME", length = 35)
    private String townName;

    @Column(name = "COUNTRY_SUB_DIVISION", length = 35)
    private String countrySubDivision;

    @Column(name = "COUNTRY", length = 2)
    private String country;

    @Column(name = "ADDRESS_LINE1", length = 70)
    private String addressLine1;

    @Column(name = "ADDRESS_LINE2", length = 70)
    private String addressLine2;

    @Column(name = "ADDRESS_LINE3", length = 70)
    private String addressLine3;
}