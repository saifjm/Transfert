package com.smi.mstr.transfer.domain.entity;

import com.smi.mstr.transfer.domain.enums.PartyRole;
import com.smi.mstr.transfer.domain.enums.PartyType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "TR_PARTY")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrParty {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tr_party_seq")
    @SequenceGenerator(
            name = "tr_party_seq",
            sequenceName = "SEQ_TR_PARTY",
            allocationSize = 1
    )
    @Column(name = "PARTY_ID")
    private Long partyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REF_OPERATION", nullable = false)
    private MvtTrOperation operation;

    @Enumerated(EnumType.STRING)
    @Column(name = "PARTY_ROLE", nullable = false, length = 20)
    private PartyRole partyRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "PARTY_TYPE", nullable = false, length = 10)
    private PartyType partyType;

    @Column(name = "NAME", nullable = false, length = 140)
    private String name;

    @Column(name = "LOCAL_PARTY_ID", length = 50)
    private String localPartyId;

    @Column(name = "LOCAL_ID_TYPE", length = 30)
    private String localIdType;

    @Column(name = "CUSTOMER_CODE", length = 50)
    private String customerCode;

    @Column(name = "COUNTRY_OF_RESIDENCE", length = 2)
    private String countryOfResidence;

    @Column(name = "BIRTH_DATE")
    private LocalDate birthDate;

    @Column(name = "CITY_OF_BIRTH", length = 35)
    private String cityOfBirth;

    @Column(name = "COUNTRY_OF_BIRTH", length = 2)
    private String countryOfBirth;

    @OneToMany(
            mappedBy = "party",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<TrPartyPostalAddress> postalAddresses = new ArrayList<>();

    @OneToMany(
            mappedBy = "party",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<TrPartyIdentification> identifications = new ArrayList<>();

    public void addAddress(TrPartyPostalAddress address) {
        postalAddresses.add(address);
        address.setParty(this);
    }

    public void addIdentification(TrPartyIdentification identification) {
        identifications.add(identification);
        identification.setParty(this);
    }
}
