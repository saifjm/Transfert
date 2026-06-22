package com.smi.mstr.transfer.domain.entity;

import com.smi.mstr.transfer.domain.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "TR_INTERBANK_ENRICHMENT", schema = "TRSF")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrInterbankEnrichment {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "seq_tr_interbank_enrichment"
    )
    @SequenceGenerator(
            name = "seq_tr_interbank_enrichment",
            sequenceName = "TRSF.SEQ_TR_INTERBANK_ENRICHMENT",
            allocationSize = 1
    )
    @Column(name = "ENRICHMENT_ID")
    private Long enrichmentId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REF_OPERATION", nullable = false)
    private MvtTrOperation operation;

    @Enumerated(EnumType.STRING)
    @Column(name = "ENRICHMENT_STATUS", nullable = false, length = 40)
    private InterbankEnrichmentStatus enrichmentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "PAYMENT_PATH_STATUS", nullable = false, length = 40)
    private PaymentPathStatus paymentPathStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "PAYMENT_ROUTE_TYPE", nullable = false, length = 40)
    private PaymentRouteType paymentRouteType;

    @Enumerated(EnumType.STRING)
    @Column(name = "SETTLEMENT_METHOD", length = 20)
    private SettlementMethod settlementMethod;

    @Column(name = "SETTLEMENT_ACCOUNT_REF", length = 100)
    private String settlementAccountRef;

    @Column(name = "SETTLEMENT_CURRENCY", length = 3)
    private String settlementCurrency;

    @Column(name = "SETTLEMENT_AMOUNT", precision = 18, scale = 3)
    private BigDecimal settlementAmount;

    @Column(name = "SETTLEMENT_DATE")
    private LocalDate settlementDate;

    @Column(name = "NOSTRO_ACCOUNT_REF", length = 100)
    private String nostroAccountRef;

    @Column(name = "NOSTRO_CURRENCY", length = 3)
    private String nostroCurrency;

    @Column(name = "NOSTRO_AGENT_BIC", length = 11)
    private String nostroAgentBic;

    @Column(name = "COVER_REQUIRED", nullable = false, length = 1)
    private String coverRequired;

    @Enumerated(EnumType.STRING)
    @Column(name = "COVER_MESSAGE_TYPE", nullable = false, length = 20)
    private CoverMessageType coverMessageType;

    @Column(name = "COVER_REASON", length = 500)
    private String coverReason;

    @Column(name = "PACS008_MESSAGE_ID", length = 35)
    private String pacs008MessageId;

    @Column(name = "PACS009_COV_MESSAGE_ID", length = 35)
    private String pacs009CovMessageId;

    @Column(name = "UETR", length = 36)
    private String uetr;

    /*
     * MS-REF proposal tracking
     */

    @Column(name = "REF_PROPOSAL_ID", length = 100)
    private String refProposalId;

    @Column(name = "REF_SOURCE_SYSTEM", length = 30)
    private String refSourceSystem;

    @Column(name = "REF_VERSION", length = 50)
    private String refVersion;

    @Column(name = "REF_FETCHED_AT")
    private LocalDateTime refFetchedAt;

    /*
     * Manual override tracking
     */

    @Column(name = "MANUAL_OVERRIDE", nullable = false, length = 1)
    private String manualOverride;

    @Column(name = "OVERRIDE_REASON", length = 500)
    private String overrideReason;

    /*
     * Chain snapshots
     */

    @Lob
    @Column(name = "DEFAULT_CHAIN_JSON")
    private String defaultChainJson;

    @Lob
    @Column(name = "FINAL_CHAIN_JSON")
    private String finalChainJson;

    /*
     * Control / audit
     */

    @Column(name = "LAST_CONTROL_STATUS", length = 40)
    private String lastControlStatus;

    @Column(name = "LAST_CONTROL_AT")
    private LocalDateTime lastControlAt;

    @Column(name = "ENRICHED_BY", length = 100)
    private String enrichedBy;

    @Column(name = "ENRICHED_AT")
    private LocalDateTime enrichedAt;

    @Lob
    @Column(name = "SNAPSHOT_JSON")
    private String snapshotJson;

    @Lob
    @Column(name = "WARNING_JSON")
    private String warningJson;

    public boolean isCoverRequired() {
        return "O".equalsIgnoreCase(coverRequired);
    }

    public boolean isManualOverride() {
        return "O".equalsIgnoreCase(manualOverride);
    }
}