package com.smi.mstr.transfer.application;

import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.domain.entity.TrPaymentModality;
import com.smi.mstr.transfer.domain.entity.TrPaymentSecurity;
import com.smi.mstr.transfer.domain.enums.OperationEventType;
import com.smi.mstr.transfer.domain.enums.PaymentImpactAction;
import com.smi.mstr.transfer.domain.enums.PaymentImpactTarget;
import com.smi.mstr.transfer.domain.enums.PaymentResourceAvailabilityStatus;
import com.smi.mstr.transfer.domain.enums.PaymentResourceType;
import com.smi.mstr.transfer.domain.enums.PaymentSecurityStatus;
import com.smi.mstr.transfer.domain.enums.ValidationSeverity;
import com.smi.mstr.transfer.domain.repository.MvtTrOperationRepository;
import com.smi.mstr.transfer.dto.payment.PaymentModalityValidatorViewDto;
import com.smi.mstr.transfer.dto.payment.PaymentSecurityItemDto;
import com.smi.mstr.transfer.dto.payment.PaymentValidationAnomalyDto;
import com.smi.mstr.transfer.dto.payment.PaymentValidatorReviewReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferPaymentValidatorReviewService {

    private static final String VALIDATOR_ROLE = "VALIDATEUR";

    private final MvtTrOperationRepository operationRepository;
    private final TransferOperationEventService eventService;
    private final TransferOperationLookupService operationLookupService;

    /**
     * PB-15 — Vue validateur du résultat de disponibilité / sécurisation.
     *
     * operationRef côté API correspond maintenant à REF_ORDRE côté DB.
     */
    @Transactional
    public PaymentValidatorReviewReport getValidatorReview(
            String operationRef,
            String viewedBy
    ) {
        MvtTrOperation operation = operationLookupService.findByReference(operationRef);

        LocalDateTime viewedAt = LocalDateTime.now();

        List<PaymentValidationAnomalyDto> anomalies = new ArrayList<>();

        List<TrPaymentModality> modalities = operation.getPaymentModalities() == null
                ? List.of()
                : operation.getPaymentModalities();

        if (modalities.isEmpty()) {
            anomalies.add(new PaymentValidationAnomalyDto(
                    ValidationSeverity.BLOCKING,
                    "PAYMENT_MODALITIES_MISSING",
                    null,
                    null,
                    "payment.modalities",
                    "No payment modality has been entered."
            ));
        }

        checkSharePercentTotal(modalities, anomalies);

        List<PaymentModalityValidatorViewDto> modalityViews = modalities.stream()
                .map(modality -> toValidatorView(modality, anomalies))
                .toList();

        PaymentResourceAvailabilityStatus overallAvailabilityStatus =
                resolveOverallAvailability(modalities);

        PaymentSecurityStatus overallSecurityStatus =
                resolveOverallSecurity(modalities);

        boolean canProceedToValidation = anomalies.stream()
                .noneMatch(anomaly -> anomaly.severity() == ValidationSeverity.BLOCKING);

        eventService.registerEvent(
                operation,
                OperationEventType.PAYMENT_VALIDATION_RESULT_VIEWED,
                operation.getStatus(),
                operation.getStatus(),
                resolveActor(viewedBy),
                VALIDATOR_ROLE,
                "Payment validation result viewed. Can proceed: " + canProceedToValidation,
                null
        );

        return new PaymentValidatorReviewReport(
                operation.getRefOrdre(),
                operation.getMntDevise(),
                operation.getCodeDevise(),
                overallAvailabilityStatus,
                overallSecurityStatus,
                canProceedToValidation,
                viewedAt,
                modalityViews,
                anomalies
        );
    }

    private PaymentModalityValidatorViewDto toValidatorView(
            TrPaymentModality modality,
            List<PaymentValidationAnomalyDto> anomalies
    ) {
        PaymentResourceType resourceType = resolveResourceType(modality);
        PaymentImpactTarget impactTarget = resolveImpactTarget(modality);
        PaymentImpactAction impactAction = resolveImpactAction(modality);
        String resourceRef = resolveResourceRef(modality);

        checkAvailabilityAnomalies(modality, anomalies);
        checkSecurityAnomalies(modality, anomalies);

        /*
         * securities est chargé lazy ici, dans la transaction.
         * Ne pas fetcher paymentModalities + paymentModalities.securities
         * dans le même EntityGraph si ce sont deux List.
         */
        List<PaymentSecurityItemDto> securityItems =
                modality.getSecurities() == null
                        ? List.of()
                        : modality.getSecurities()
                          .stream()
                          .map(this::toSecurityDto)
                          .toList();

        return new PaymentModalityValidatorViewDto(
                modality.getModalityId(),
                modality.getSequenceNo(),
                modality.getModalityType(),

                modality.getSharePercent(),

                modality.getTargetAmount(),
                modality.getTargetCurrency(),

                resourceType,
                impactTarget,
                impactAction,
                resourceRef,

                modality.getAvailabilityStatus(),
                modality.getAvailableAmount(),
                modality.getAvailableCurrency(),
                modality.getAvailabilityCheckedAt(),
                modality.getAvailabilityMessage(),

                modality.getSecurityStatus(),
                securityItems
        );
    }

    private void checkSharePercentTotal(
            List<TrPaymentModality> modalities,
            List<PaymentValidationAnomalyDto> anomalies
    ) {
        if (modalities == null || modalities.isEmpty()) {
            return;
        }

        BigDecimal total = modalities.stream()
                .map(TrPaymentModality::getSharePercent)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (total.compareTo(new BigDecimal("100.00")) != 0) {
            anomalies.add(new PaymentValidationAnomalyDto(
                    ValidationSeverity.BLOCKING,
                    "PAYMENT_SHARE_TOTAL_INVALID",
                    null,
                    null,
                    "payment.modalities.sharePercent",
                    "The total payment modality percentage must be equal to 100%. Current total: "
                            + total + "%"
            ));
        }
    }

    private void checkAvailabilityAnomalies(
            TrPaymentModality modality,
            List<PaymentValidationAnomalyDto> anomalies
    ) {
        PaymentResourceAvailabilityStatus status = modality.getAvailabilityStatus();

        if (status == null || status == PaymentResourceAvailabilityStatus.NOT_REQUIRED) {
            anomalies.add(new PaymentValidationAnomalyDto(
                    ValidationSeverity.BLOCKING,
                    "PAYMENT_RESOURCE_NOT_CHECKED",
                    modality.getModalityId(),
                    resolveModalityTypeName(modality),
                    fieldPath(modality, "availabilityStatus"),
                    "Payment resource availability has not been checked."
            ));
            return;
        }

        if (status == PaymentResourceAvailabilityStatus.INSUFFICIENT) {
            anomalies.add(new PaymentValidationAnomalyDto(
                    ValidationSeverity.BLOCKING,
                    "PAYMENT_RESOURCE_INSUFFICIENT",
                    modality.getModalityId(),
                    resolveModalityTypeName(modality),
                    fieldPath(modality, "availabilityStatus"),
                    "Payment resource is insufficient: " + nullSafe(modality.getAvailabilityMessage())
            ));
            return;
        }

        if (status == PaymentResourceAvailabilityStatus.UNAVAILABLE
                || status == PaymentResourceAvailabilityStatus.ERROR) {
            anomalies.add(new PaymentValidationAnomalyDto(
                    ValidationSeverity.BLOCKING,
                    "PAYMENT_RESOURCE_UNAVAILABLE",
                    modality.getModalityId(),
                    resolveModalityTypeName(modality),
                    fieldPath(modality, "availabilityStatus"),
                    "Payment resource is unavailable or in error: "
                            + nullSafe(modality.getAvailabilityMessage())
            ));
        }
    }

    private void checkSecurityAnomalies(
            TrPaymentModality modality,
            List<PaymentValidationAnomalyDto> anomalies
    ) {
        PaymentSecurityStatus status = modality.getSecurityStatus();

        if (status == null) {
            anomalies.add(new PaymentValidationAnomalyDto(
                    ValidationSeverity.WARNING,
                    "PAYMENT_SECURITY_STATUS_MISSING",
                    modality.getModalityId(),
                    resolveModalityTypeName(modality),
                    fieldPath(modality, "securityStatus"),
                    "Payment security status is missing."
            ));
            return;
        }

        if (status == PaymentSecurityStatus.REQUIRED_NOT_SECURED) {
            anomalies.add(new PaymentValidationAnomalyDto(
                    ValidationSeverity.BLOCKING,
                    "PAYMENT_RESOURCE_NOT_SECURED",
                    modality.getModalityId(),
                    resolveModalityTypeName(modality),
                    fieldPath(modality, "securityStatus"),
                    "Payment resource is required but not secured."
            ));
            return;
        }

        if (status == PaymentSecurityStatus.FAILED) {
            anomalies.add(new PaymentValidationAnomalyDto(
                    ValidationSeverity.BLOCKING,
                    "PAYMENT_SECURITY_FAILED",
                    modality.getModalityId(),
                    resolveModalityTypeName(modality),
                    fieldPath(modality, "securityStatus"),
                    "Payment resource security failed."
            ));
            return;
        }

        if (status == PaymentSecurityStatus.SECURED
                && (modality.getSecurities() == null || modality.getSecurities().isEmpty())) {
            anomalies.add(new PaymentValidationAnomalyDto(
                    ValidationSeverity.BLOCKING,
                    "PAYMENT_SECURITY_REFERENCE_MISSING",
                    modality.getModalityId(),
                    resolveModalityTypeName(modality),
                    fieldPath(modality, "securities"),
                    "Payment resource is marked as secured but no security reference exists."
            ));
        }
    }

    private PaymentSecurityItemDto toSecurityDto(TrPaymentSecurity security) {
        return new PaymentSecurityItemDto(
                security.getSecurityId(),
                security.getModality() == null
                        ? null
                        : security.getModality().getModalityId(),
                security.getResourceType(),
                security.getSecurityStatus(),
                security.getResourceRef(),
                security.getRequestedAmount(),
                security.getRequestedCurrency(),
                security.getFxRate(),
                security.getCounterValueAmount(),
                security.getCounterValueCurrency(),
                security.getEstimatedFeesAmount(),
                security.getEstimatedFeesCurrency(),
                security.getSecuredAmount(),
                security.getSecuredCurrency(),
                security.getSecurityReference(),
                security.getSecuredAt(),
                security.getSecurityMessage()
        );
    }

    private PaymentResourceAvailabilityStatus resolveOverallAvailability(
            List<TrPaymentModality> modalities
    ) {
        if (modalities == null || modalities.isEmpty()) {
            return PaymentResourceAvailabilityStatus.ERROR;
        }

        if (modalities.stream().anyMatch(m ->
                m.getAvailabilityStatus() == null
                        || m.getAvailabilityStatus() == PaymentResourceAvailabilityStatus.ERROR)) {
            return PaymentResourceAvailabilityStatus.ERROR;
        }

        if (modalities.stream().anyMatch(m ->
                m.getAvailabilityStatus() == PaymentResourceAvailabilityStatus.UNAVAILABLE)) {
            return PaymentResourceAvailabilityStatus.UNAVAILABLE;
        }

        if (modalities.stream().anyMatch(m ->
                m.getAvailabilityStatus() == PaymentResourceAvailabilityStatus.INSUFFICIENT)) {
            return PaymentResourceAvailabilityStatus.INSUFFICIENT;
        }

        if (modalities.stream().allMatch(m ->
                m.getAvailabilityStatus() == PaymentResourceAvailabilityStatus.AVAILABLE)) {
            return PaymentResourceAvailabilityStatus.AVAILABLE;
        }

        return PaymentResourceAvailabilityStatus.NOT_REQUIRED;
    }

    private PaymentSecurityStatus resolveOverallSecurity(
            List<TrPaymentModality> modalities
    ) {
        if (modalities == null || modalities.isEmpty()) {
            return PaymentSecurityStatus.NOT_REQUIRED;
        }

        if (modalities.stream().anyMatch(m ->
                m.getSecurityStatus() == PaymentSecurityStatus.FAILED)) {
            return PaymentSecurityStatus.FAILED;
        }

        if (modalities.stream().anyMatch(m ->
                m.getSecurityStatus() == PaymentSecurityStatus.REQUIRED_NOT_SECURED)) {
            return PaymentSecurityStatus.REQUIRED_NOT_SECURED;
        }

        if (modalities.stream().allMatch(m ->
                m.getSecurityStatus() == PaymentSecurityStatus.SECURED)) {
            return PaymentSecurityStatus.SECURED;
        }

        if (modalities.stream().anyMatch(m ->
                m.getSecurityStatus() == PaymentSecurityStatus.SECURED)) {
            return PaymentSecurityStatus.SECURED;
        }

        return PaymentSecurityStatus.NOT_REQUIRED;
    }

    private PaymentResourceType resolveResourceType(TrPaymentModality modality) {
        if (modality.getModalityType() == null) {
            return null;
        }

        return switch (modality.getModalityType()) {
            case TND_FX_PURCHASE_NORMAL,
                 TND_FX_PURCHASE_NEGOTIATED,
                 DIRECT_FOREIGN_CURRENCY_ACCOUNT_DEBIT,
                 CURRENCY_ARBITRAGE -> PaymentResourceType.ACCOUNT_BALANCE;

            case FORWARD_FX_CONTRACT -> PaymentResourceType.FORWARD_CONTRACT;
            case IMPORT_FINANCING -> PaymentResourceType.FINANCING_LINE;
            case FUNDS_RECEIVED_LOCAL_BANK -> PaymentResourceType.RECEIVED_FUNDS;
            case INTERBANK_FX_COVER -> PaymentResourceType.INTERBANK_COVER;
            case OTHER -> PaymentResourceType.ACCOUNT_BALANCE;
        };
    }

    private PaymentImpactTarget resolveImpactTarget(TrPaymentModality modality) {
        if (modality.getModalityType() == null) {
            return null;
        }

        return switch (modality.getModalityType()) {
            case TND_FX_PURCHASE_NORMAL,
                 TND_FX_PURCHASE_NEGOTIATED,
                 DIRECT_FOREIGN_CURRENCY_ACCOUNT_DEBIT,
                 CURRENCY_ARBITRAGE -> PaymentImpactTarget.ACCOUNT;

            case FORWARD_FX_CONTRACT -> PaymentImpactTarget.FORWARD_CONTRACT;
            case IMPORT_FINANCING -> PaymentImpactTarget.FINANCING_FOLDER;
            case FUNDS_RECEIVED_LOCAL_BANK -> PaymentImpactTarget.RECEIVED_FUNDS;
            case INTERBANK_FX_COVER -> PaymentImpactTarget.INTERBANK_COVER;
            case OTHER -> PaymentImpactTarget.ACCOUNT;
        };
    }

    private PaymentImpactAction resolveImpactAction(TrPaymentModality modality) {
        if (modality.getModalityType() == null) {
            return null;
        }

        return switch (modality.getModalityType()) {
            case TND_FX_PURCHASE_NORMAL,
                 TND_FX_PURCHASE_NEGOTIATED,
                 DIRECT_FOREIGN_CURRENCY_ACCOUNT_DEBIT,
                 CURRENCY_ARBITRAGE -> PaymentImpactAction.BLOCK_AMOUNT;

            case FORWARD_FX_CONTRACT -> PaymentImpactAction.RESERVE_CONTRACT;
            case IMPORT_FINANCING -> PaymentImpactAction.RESERVE_FINANCING_AMOUNT;
            case FUNDS_RECEIVED_LOCAL_BANK -> PaymentImpactAction.RESERVE_RECEIVED_FUNDS;
            case INTERBANK_FX_COVER -> PaymentImpactAction.RESERVE_INTERBANK_COVER;
            case OTHER -> PaymentImpactAction.BLOCK_AMOUNT;
        };
    }

    private String resolveResourceRef(TrPaymentModality modality) {
        if (modality.getModalityType() == null) {
            return null;
        }

        return switch (modality.getModalityType()) {
            case TND_FX_PURCHASE_NORMAL,
                 TND_FX_PURCHASE_NEGOTIATED,
                 DIRECT_FOREIGN_CURRENCY_ACCOUNT_DEBIT,
                 CURRENCY_ARBITRAGE -> modality.getDebitAccountRef();

            case FORWARD_FX_CONTRACT -> modality.getForwardContractRef();
            case IMPORT_FINANCING -> modality.getFinancingRef();
            case FUNDS_RECEIVED_LOCAL_BANK -> modality.getReceivedFundsRef();
            case INTERBANK_FX_COVER -> modality.getInterbankCoverRef();
            case OTHER -> modality.getDebitAccountRef();
        };
    }



    private String resolveActor(String viewedBy) {
        return viewedBy == null || viewedBy.isBlank()
                ? "SYSTEM"
                : viewedBy;
    }

    private String resolveModalityTypeName(TrPaymentModality modality) {
        return modality.getModalityType() == null
                ? null
                : modality.getModalityType().name();
    }

    private String fieldPath(TrPaymentModality modality, String fieldName) {
        return "payment.modalities[" + modality.getSequenceNo() + "]." + fieldName;
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}