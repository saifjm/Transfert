package com.smi.mstr.transfer.domain.entity;

import com.smi.mstr.transfer.domain.enums.AccountRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TR_ACCOUNT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tr_account_seq")
    @SequenceGenerator(
            name = "tr_account_seq",
            sequenceName = "SEQ_TR_ACCOUNT",
            allocationSize = 1
    )
    @Column(name = "ACCOUNT_ID")
    private Long accountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REF_OPERATION", nullable = false)
    private MvtTrOperation operation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARTY_ID")
    private TrParty party;

    @Enumerated(EnumType.STRING)
    @Column(name = "ACCOUNT_ROLE", nullable = false, length = 20)
    private AccountRole accountRole;

    @Column(name = "IBAN", length = 34)
    private String iban;

    @Column(name = "OTHER_ACCOUNT_ID", length = 100)
    private String otherAccountId;

    @Column(name = "ACCOUNT_SCHEME", length = 35)
    private String accountScheme;

    @Column(name = "ACCOUNT_CURRENCY", length = 3)
    private String accountCurrency;

    @Column(name = "ACCOUNT_NAME", length = 140)
    private String accountName;

    @Column(name = "CORE_ACCOUNT_ID", length = 100)
    private String coreAccountId;

    @Column(name = "RIB_LOCAL", length = 30)
    private String ribLocal;
}