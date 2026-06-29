package com.smi.mstr.transfer.dto;

import com.smi.mstr.transfer.domain.enums.BlockingStatus;
import com.smi.mstr.transfer.domain.enums.FxType;
import com.smi.mstr.transfer.domain.enums.InterbankRouteType;
import com.smi.mstr.transfer.domain.enums.OriginChannel;
import com.smi.mstr.transfer.domain.enums.PartyRole;
import com.smi.mstr.transfer.domain.enums.PartyType;
import com.smi.mstr.transfer.domain.enums.PaymentImpactStatus;
import com.smi.mstr.transfer.domain.enums.PaymentModalityStatus;
import com.smi.mstr.transfer.domain.enums.PaymentModalityType;
import com.smi.mstr.transfer.domain.enums.PaymentResourceType;
import com.smi.mstr.transfer.domain.enums.ResidencyStatus;
import com.smi.mstr.transfer.domain.enums.StatutImputationSupport;
import com.smi.mstr.transfer.domain.enums.StatutReservationSupport;
import com.smi.mstr.transfer.domain.enums.StatutSupportReglementaire;
import com.smi.mstr.transfer.domain.enums.StatutValidationSupport;
import com.smi.mstr.transfer.domain.enums.SwiftPriority;
import com.smi.mstr.transfer.domain.enums.TransferOperationStatus;
import com.smi.mstr.transfer.domain.enums.TransferType;
import com.smi.mstr.transfer.domain.enums.TypeSupportReglementaire;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record TransferOperationResponse(

        /*
         * Main identifiers
         */
        Long refOperation,
        String operationRef,
        String refOrdre,
        String numDossier,

        /*
         * Dates
         */
        LocalDate dateOperation,
        LocalDate dateDossier,
        LocalDate dateValidation,
        LocalDateTime createdAt,

        /*
         * Workflow / source
         */
        String codeAgence,
        OriginChannel sourceChannel,
        String sourceModule,
        String sourceReference,
        String workflowInstanceId,
        String workflowTaskId,
        String correlationId,

        /*
         * Transfer classification
         */
        TransferType typeTransfert,
        Long codeOperation,
        String codeNatureOperation,

        /*
         * Regulatory dossier summary
         */
        String numAutorisationBct,
        LocalDate dateAutorisationBct,
        String typeDossierReg,
        String numDossierReg,
        LocalDate dateDossierReg,

        /*
         * ISO / SWIFT references
         */
        String endToEndId,
        String transactionId,
        String uetr,

        /*
         * Amounts
         */
        BigDecimal mntOrdre,
        String codeDeviseOrdre,
        BigDecimal mntDevise,
        String codeDevise,
        LocalDate dateValeurTransfert,
        BigDecimal coursConversion,
        BigDecimal contreValeurTnd,

        /*
         * Payment instruction
         */
        SwiftPriority swiftPriority,
        String serviceLevelCode,
        String localInstrumentCode,
        String categoryPurposeCode,
        String purposeCode,
        String purposeProprietary,
        String remittanceUnstructured,
        String chargeBearer,

        /*
         * Interbank summary
         */
        InterbankRouteType routeType,
        String coverRequired,
        String settlementMethod,
        String settlementAccountRef,

        /*
         * Status
         */
        TransferOperationStatus status,

        /*
         * Children
         */
        List<PartyResponse> parties,
        List<PaymentModalityResponse> paymentModalities,
        List<RegulatorySupportResponse> regulatorySupports
) {

    public TransferOperationResponse {
        parties = parties == null ? List.of() : List.copyOf(parties);
        paymentModalities = paymentModalities == null ? List.of() : List.copyOf(paymentModalities);
        regulatorySupports = regulatorySupports == null ? List.of() : List.copyOf(regulatorySupports);
    }

    public boolean editable() {
        return status != null && status.editable();
    }

    public boolean validated() {
        return status != null && status.isValidated();
    }

    public boolean executed() {
        return status != null && status.isAppliedOrExecuted();
    }

    public boolean cancelled() {
        return status != null && status.isCancelled();
    }

    public boolean rejected() {
        return status != null && status.isRejected();
    }

    public boolean coverRequiredFlag() {
        return "Y".equalsIgnoreCase(coverRequired)
                || "O".equalsIgnoreCase(coverRequired);
    }

    public record PartyResponse(

            Long idParty,
            Integer sequenceNo,

            PartyRole partyRole,
            PartyType partyType,

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

            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {

        public boolean customerParty() {
            return partyRole != null && partyRole.isCustomerParty();
        }

        public boolean account() {
            return partyRole != null && partyRole.isAccount();
        }

        public boolean financialAgent() {
            return partyRole != null && partyRole.isFinancialAgent();
        }

        public String accountReference() {
            if (accountIban != null && !accountIban.isBlank()) {
                return accountIban;
            }

            if (accountNumber != null && !accountNumber.isBlank()) {
                return accountNumber;
            }

            return externalPartyRef;
        }

        public String bankReference() {
            if (bic != null && !bic.isBlank()) {
                return bic;
            }

            if (bankCode != null && !bankCode.isBlank()) {
                return bankCode;
            }

            return clearingMemberId;
        }
    }

    public record PaymentModalityResponse(

            Long idPaymentModality,
            Integer sequenceNo,

            PaymentModalityType modalityType,

            BigDecimal coveragePercentage,
            BigDecimal coveredTransferAmount,
            String coveredTransferCurrency,

            String debitAccountNumber,
            String debitAccountCurrency,
            BigDecimal debitAmount,

            String fxRequired,
            FxType fxType,
            BigDecimal fxRate,
            LocalDateTime fxRateDate,
            String fxReference,

            PaymentResourceType resourceType,
            String resourceReference,

            String blockingRequired,
            BlockingStatus blockingStatus,
            String blockingReference,
            BigDecimal blockedAmount,
            String blockedCurrency,
            LocalDateTime blockedAt,

            PaymentImpactStatus impactStatus,
            String impactReference,
            LocalDateTime impactedAt,

            PaymentModalityStatus modalityStatus,

            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {

        public boolean requiresFx() {
            return "Y".equalsIgnoreCase(fxRequired)
                    || "O".equalsIgnoreCase(fxRequired);
        }

        public boolean requiresBlocking() {
            return "Y".equalsIgnoreCase(blockingRequired)
                    || "O".equalsIgnoreCase(blockingRequired);
        }

        public boolean blocked() {
            return blockingStatus == BlockingStatus.BLOCKED;
        }

        public boolean executed() {
            return impactStatus == PaymentImpactStatus.EXECUTED;
        }

        public boolean accountBased() {
            return resourceType != null && resourceType.isAccountBased();
        }

        public boolean financingBased() {
            return resourceType != null && resourceType.isFinancingBased();
        }

        public boolean interbankCoverBased() {
            return resourceType != null && resourceType.isInterbankCoverBased();
        }
    }

    public record RegulatorySupportResponse(

            Long idSupport,
            Integer sequenceNo,

            TypeSupportReglementaire typeSupport,
            Integer codeSupportBct,

            String numSupport,
            LocalDate dateSupport,
            String autoriteEmettrice,

            String numIdentification,
            LocalDate dateIdentification,

            String codeRd,
            Integer modeReglement,
            String numMessageSwift,
            String codeBanque,

            String deviseSupport,
            BigDecimal montantAutorise,
            BigDecimal montantUtiliseAvant,
            BigDecimal montantReserve,
            BigDecimal montantUtiliseCourant,
            BigDecimal reliquatAvant,
            BigDecimal reliquatApres,
            BigDecimal montantTnd,
            BigDecimal coursConversion,

            StatutSupportReglementaire statutSupport,
            StatutValidationSupport statutValidation,
            StatutReservationSupport statutReservation,
            StatutImputationSupport statutImputation,

            String messageValidation,

            String sourceSystem,
            String sourceReference,

            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {

        public boolean validated() {
            return statutValidation == StatutValidationSupport.VALIDATED
                    || statutValidation == StatutValidationSupport.NOT_REQUIRED;
        }

        public boolean reserved() {
            return statutReservation == StatutReservationSupport.RESERVED
                    || statutReservation == StatutReservationSupport.NOT_REQUIRED;
        }

        public boolean imputed() {
            return statutImputation == StatutImputationSupport.APPLIED
                    || statutImputation == StatutImputationSupport.NOT_REQUIRED;
        }

        public boolean tce() {
            return typeSupport == TypeSupportReglementaire.TCE;
        }

        public boolean ficheInformation() {
            return typeSupport == TypeSupportReglementaire.FICHE_INFORMATION;
        }

        public boolean hasRemainingAmount() {
            return reliquatApres != null
                    && reliquatApres.compareTo(BigDecimal.ZERO) > 0;
        }
    }
}