package com.smi.mstr.transfer.application.mapper;

import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.domain.entity.TrParty;
import com.smi.mstr.transfer.domain.entity.TrPaymentModality;
import com.smi.mstr.transfer.domain.entity.TrSupportReglementaire;
import com.smi.mstr.transfer.dto.TransferOperationResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class TransferOperationResponseMapper {

    public TransferOperationResponse toResponse(MvtTrOperation operation) {
        if (operation == null) {
            return null;
        }

        return new TransferOperationResponse(
                operation.getRefOperation(),
                resolveOperationRef(operation),
                operation.getRefOrdre(),
                operation.getNumDossier(),

                operation.getDateOperation(),
                operation.getDateDossier(),
                operation.getDateValidation(),
                operation.getCreatedAt(),

                operation.getCodeAgence(),
                operation.getSourceChannel(),
                operation.getSourceModule(),
                operation.getSourceReference(),
                operation.getWorkflowInstanceId(),
                operation.getWorkflowTaskId(),
                operation.getCorrelationId(),

                operation.getTypeTransfert(),
                operation.getCodeOperation(),
                operation.getCodeNatureOperation(),

                operation.getNumAutorisationBct(),
                operation.getDateAutorisationBct(),
                operation.getTypeDossierReg(),
                operation.getNumDossierReg(),
                operation.getDateDossierReg(),

                operation.getEndToEndId(),
                operation.getTransactionId(),
                operation.getUetr(),

                operation.getMntOrdre(),
                operation.getCodeDeviseOrdre(),
                operation.getMntDevise(),
                operation.getCodeDevise(),
                operation.getDateValeurTransfert(),
                operation.getCoursConversion(),
                operation.getContreValeurTnd(),

                operation.getSwiftPriority(),
                operation.getServiceLevelCode(),
                operation.getLocalInstrumentCode(),
                operation.getCategoryPurposeCode(),
                operation.getPurposeCode(),
                operation.getPurposeProprietary(),
                operation.getRemittanceUnstructured(),
                operation.getChargeBearer(),

                operation.getRouteType(),
                operation.getCoverRequired(),
                operation.getSettlementMethod(),
                operation.getSettlementAccountRef(),

                operation.getStatus(),

                mapParties(operation.getParties()),
                mapPaymentModalities(operation.getPaymentModalities()),
                mapRegulatorySupports(operation.getSupportsReglementaires())
        );
    }

    public List<TransferOperationResponse> toResponseList(
            List<MvtTrOperation> operations
    ) {
        if (operations == null || operations.isEmpty()) {
            return List.of();
        }

        return operations.stream()
                .map(this::toResponse)
                .toList();
    }

    public Page<TransferOperationResponse> toResponsePage(
            Page<MvtTrOperation> operations
    ) {
        if (operations == null) {
            return Page.empty();
        }

        return operations.map(this::toResponse);
    }

    private List<TransferOperationResponse.PartyResponse> mapParties(
            List<TrParty> parties
    ) {
        if (parties == null || parties.isEmpty()) {
            return List.of();
        }

        return parties.stream()
                .sorted(
                        Comparator
                                .comparing(
                                        TrParty::getPartyRole,
                                        Comparator.nullsLast(Comparator.naturalOrder())
                                )
                                .thenComparing(
                                        TrParty::getSequenceNo,
                                        Comparator.nullsLast(Comparator.naturalOrder())
                                )
                )
                .map(this::mapParty)
                .toList();
    }

    private TransferOperationResponse.PartyResponse mapParty(TrParty party) {
        return new TransferOperationResponse.PartyResponse(
                party.getIdParty(),
                party.getSequenceNo(),

                party.getPartyRole(),
                party.getPartyType(),

                party.getCustomerId(),
                party.getExternalPartyRef(),
                party.getName(),
                party.getCountryCode(),
                party.getResidencyStatus(),

                party.getIdentificationType(),
                party.getIdentificationValue(),
                party.getIdentificationIssuer(),
                party.getIdentificationScheme(),
                party.getLei(),

                party.getAddressLine1(),
                party.getAddressLine2(),
                party.getAddressLine3(),
                party.getTownName(),
                party.getPostCode(),
                party.getCountrySubDivision(),

                party.getAccountNumber(),
                party.getAccountIban(),
                party.getAccountScheme(),
                party.getAccountCurrency(),
                party.getAccountType(),
                party.getAccountName(),

                party.getBic(),
                party.getBankCode(),
                party.getBankName(),
                party.getBankBranchCode(),
                party.getBankBranchName(),
                party.getBankCountryCode(),
                party.getClearingSystemCode(),
                party.getClearingMemberId(),

                party.getAgentPosition(),
                party.getRoutingRole(),

                party.getSourceSystem(),
                party.getSourceReference(),

                party.getCreatedAt(),
                party.getUpdatedAt()
        );
    }

    private List<TransferOperationResponse.PaymentModalityResponse> mapPaymentModalities(
            List<TrPaymentModality> modalities
    ) {
        if (modalities == null || modalities.isEmpty()) {
            return List.of();
        }

        return modalities.stream()
                .sorted(
                        Comparator.comparing(
                                TrPaymentModality::getSequenceNo,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        )
                )
                .map(this::mapPaymentModality)
                .toList();
    }

    private TransferOperationResponse.PaymentModalityResponse mapPaymentModality(
            TrPaymentModality modality
    ) {
        return new TransferOperationResponse.PaymentModalityResponse(
                modality.getIdPaymentModality(),
                modality.getSequenceNo(),

                modality.getModalityType(),

                modality.getCoveragePercentage(),
                modality.getCoveredTransferAmount(),
                modality.getCoveredTransferCurrency(),

                modality.getDebitAccountNumber(),
                modality.getDebitAccountCurrency(),
                modality.getDebitAmount(),

                modality.getFxRequired(),
                modality.getFxType(),
                modality.getFxRate(),
                modality.getFxRateDate(),
                modality.getFxReference(),

                modality.getResourceType(),
                modality.getResourceReference(),

                modality.getBlockingRequired(),
                modality.getBlockingStatus(),
                modality.getBlockingReference(),
                modality.getBlockedAmount(),
                modality.getBlockedCurrency(),
                modality.getBlockedAt(),

                modality.getImpactStatus(),
                modality.getImpactReference(),
                modality.getImpactedAt(),

                modality.getModalityStatus(),

                modality.getCreatedAt(),
                modality.getUpdatedAt()
        );
    }

    private List<TransferOperationResponse.RegulatorySupportResponse> mapRegulatorySupports(
            List<TrSupportReglementaire> supports
    ) {
        if (supports == null || supports.isEmpty()) {
            return List.of();
        }

        return supports.stream()
                .sorted(
                        Comparator.comparing(
                                TrSupportReglementaire::getSequenceNo,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        )
                )
                .map(this::mapRegulatorySupport)
                .toList();
    }

    private TransferOperationResponse.RegulatorySupportResponse mapRegulatorySupport(
            TrSupportReglementaire support
    ) {
        return new TransferOperationResponse.RegulatorySupportResponse(
                support.getIdSupport(),
                support.getSequenceNo(),

                support.getTypeSupport(),
                support.getCodeSupportBct(),

                support.getNumSupport(),
                support.getDateSupport(),
                support.getAutoriteEmettrice(),

                support.getNumIdentification(),
                support.getDateIdentification(),

                support.getCodeRd(),
                support.getModeReglement(),
                support.getNumMessageSwift(),
                support.getCodeBanque(),

                support.getDeviseSupport(),
                support.getMontantAutorise(),
                support.getMontantUtiliseAvant(),
                support.getMontantReserve(),
                support.getMontantUtiliseCourant(),
                support.getReliquatAvant(),
                support.getReliquatApres(),
                support.getMontantTnd(),
                support.getCoursConversion(),

                support.getStatutSupport(),
                support.getStatutValidation(),
                support.getStatutReservation(),
                support.getStatutImputation(),

                support.getMessageValidation(),

                support.getSourceSystem(),
                support.getSourceReference(),

                support.getCreatedAt(),
                support.getUpdatedAt()
        );
    }

    private String resolveOperationRef(MvtTrOperation operation) {
        if (operation.getRefOrdre() != null && !operation.getRefOrdre().isBlank()) {
            return operation.getRefOrdre();
        }

        if (operation.getRefOperation() != null) {
            return String.valueOf(operation.getRefOperation());
        }

        return null;
    }
}