package com.smi.mstr.transfer.domain.entity;

import com.smi.mstr.transfer.domain.enums.FinancialAgentRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TR_FINANCIAL_AGENT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrFinancialAgent {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tr_fin_agent_seq")
    @SequenceGenerator(
            name = "tr_fin_agent_seq",
            sequenceName = "SEQ_TR_FINANCIAL_AGENT",
            allocationSize = 1
    )
    @Column(name = "AGENT_ID")
    private Long agentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REF_OPERATION", nullable = false)
    private MvtTrOperation operation;

    @Enumerated(EnumType.STRING)
    @Column(name = "AGENT_ROLE", nullable = false, length = 20)
    private FinancialAgentRole agentRole;

    @Column(name = "BICFI", length = 11)
    private String bicfi;

    @Column(name = "LEI", length = 20)
    private String lei;

    @Column(name = "CLEARING_SYSTEM_CODE", length = 10)
    private String clearingSystemCode;

    @Column(name = "CLEARING_MEMBER_ID", length = 35)
    private String clearingMemberId;

    @Column(name = "AGENT_NAME", length = 140)
    private String agentName;

    @Column(name = "BRANCH_ID", length = 35)
    private String branchId;

    @Column(name = "BRANCH_NAME", length = 140)
    private String branchName;

    @Column(name = "COUNTRY", length = 2)
    private String country;

    @Column(name = "ADDRESS_LINE1", length = 70)
    private String addressLine1;

    @Column(name = "ADDRESS_LINE2", length = 70)
    private String addressLine2;

    @Column(name = "TOWN_NAME", length = 35)
    private String townName;
}