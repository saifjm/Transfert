package com.smi.mstr.transfer.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TR_PARTY_IDENTIFICATION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrPartyIdentification {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tr_party_ident_seq")
    @SequenceGenerator(
            name = "tr_party_ident_seq",
            sequenceName = "SEQ_TR_PARTY_IDENT",
            allocationSize = 1
    )
    @Column(name = "PARTY_IDENT_ID")
    private Long partyIdentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARTY_ID", nullable = false)
    private TrParty party;

    @Column(name = "IDENTIFICATION_SCOPE", nullable = false, length = 10)
    private String identificationScope; // ORG_ID / PRVT_ID

    @Column(name = "IDENTIFICATION_TYPE", nullable = false, length = 35)
    private String identificationType; // TXID, NIDN, CCPT, CUST, LEI...

    @Column(name = "IDENTIFICATION_VALUE", nullable = false, length = 100)
    private String identificationValue;

    @Column(name = "ISSUER", length = 35)
    private String issuer;

    @Column(name = "SCHEME_NAME_CODE", length = 35)
    private String schemeNameCode;

    @Column(name = "SCHEME_NAME_PROPRIETARY", length = 35)
    private String schemeNameProprietary;
}
