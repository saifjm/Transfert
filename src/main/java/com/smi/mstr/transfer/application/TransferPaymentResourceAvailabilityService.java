package com.smi.mstr.transfer.application;

import com.smi.mstr.transfer.application.payment.PaymentResourceAvailabilityClient;
import com.smi.mstr.transfer.application.payment.PaymentResourceCommand;
import com.smi.mstr.transfer.application.payment.strategy.PaymentModalityHandlerRegistry;
import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.domain.entity.TrPaymentModality;
import com.smi.mstr.transfer.domain.enums.OperationEventType;
import com.smi.mstr.transfer.domain.enums.PaymentResourceAvailabilityStatus;
import com.smi.mstr.transfer.domain.enums.TransferOperationStatus;
import com.smi.mstr.transfer.domain.repository.MvtTrOperationRepository;
import com.smi.mstr.transfer.dto.payment.CheckPaymentResourceAvailabilityRequest;
import com.smi.mstr.transfer.dto.payment.PaymentResourceAvailabilityItemDto;
import com.smi.mstr.transfer.dto.payment.PaymentResourceAvailabilityReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferPaymentResourceAvailabilityService {

    private static final String AGENT_SAISIE_ROLE = "AGENT_SAISIE";

    private final MvtTrOperationRepository operationRepository;
    private final PaymentResourceAvailabilityClient availabilityClient;
    private final TransferOperationEventService eventService;
    private final PaymentModalityHandlerRegistry handlerRegistry;
    private final TransferOperationLookupService operationLookupService;

    /**
     * PB-12 — Vérifier la disponibilité des ressources de paiement.
     *
     * Le paramètre operationRef est conservé côté API pour compatibilité,
     * mais il correspond maintenant à REF_ORDRE côté base.
     */
    @Transactional
    public PaymentResourceAvailabilityReport checkAvailability(
            String operationRef,
            CheckPaymentResourceAvailabilityRequest request
    ) {
        MvtTrOperation operation = operationLookupService.findByReference(operationRef);

        assertEditable(operation);

        LocalDateTime checkedAt = LocalDateTime.now();

        List<TrPaymentModality> modalities = operation.getPaymentModalities() == null
                ? List.of()
                : operation.getPaymentModalities();

        List<PaymentResourceAvailabilityItemDto> results = modalities.stream()
                .map(modality -> checkOne(operation, modality, checkedAt))
                .toList();

        PaymentResourceAvailabilityStatus overallStatus =
                resolveOverallStatus(results);

        /*
         * Pas de operation.setUpdatedAt(...)
         * Le nouveau modèle TR_OPERATION_MVT ne contient plus UPDATED_AT.
         *
         * Les champs de TR_PAYMENT_MODALITY sont mis à jour par dirty checking JPA
         * dans la transaction courante.
         */
        operationRepository.save(operation);

        eventService.registerEvent(
                operation,
                OperationEventType.PAYMENT_RESOURCE_AVAILABILITY_CHECKED,
                TransferOperationStatus.X,
                TransferOperationStatus.X,
                request.checkedBy(),
                AGENT_SAISIE_ROLE,
                "Payment resource availability checked: " + overallStatus,
                null
        );

        return new PaymentResourceAvailabilityReport(
                operation.getRefOrdre(),
                overallStatus,
                checkedAt,
                results
        );
    }

    private PaymentResourceAvailabilityItemDto checkOne(
            MvtTrOperation operation,
            TrPaymentModality modality,
            LocalDateTime checkedAt
    ) {
        if (modality.getModalityType() == null) {
            PaymentResourceAvailabilityItemDto result =
                    new PaymentResourceAvailabilityItemDto(
                            modality.getModalityId(),
                            null,
                            null,
                            modality.getTargetAmount(),
                            modality.getTargetCurrency(),
                            null,
                            null,
                            PaymentResourceAvailabilityStatus.ERROR,
                            "Payment modality type is missing."
                    );

            applyResultToModality(modality, result, checkedAt);
            return result;
        }

        PaymentResourceCommand command = handlerRegistry
                .getHandler(modality.getModalityType())
                .buildAvailabilityCommand(operation, modality);

        PaymentResourceAvailabilityItemDto result =
                availabilityClient.check(command);

        applyResultToModality(modality, result, checkedAt);

        return result;
    }

    private void applyResultToModality(
            TrPaymentModality modality,
            PaymentResourceAvailabilityItemDto result,
            LocalDateTime checkedAt
    ) {
        modality.setAvailabilityStatus(result.status());
        modality.setAvailableAmount(result.availableAmount());
        modality.setAvailableCurrency(result.availableCurrency());
        modality.setAvailabilityCheckedAt(checkedAt);
        modality.setAvailabilityMessage(result.message());
    }

    private PaymentResourceAvailabilityStatus resolveOverallStatus(
            List<PaymentResourceAvailabilityItemDto> results
    ) {
        if (results == null || results.isEmpty()) {
            return PaymentResourceAvailabilityStatus.NOT_REQUIRED;
        }

        if (results.stream().anyMatch(r ->
                r.status() == PaymentResourceAvailabilityStatus.ERROR)) {
            return PaymentResourceAvailabilityStatus.ERROR;
        }

        if (results.stream().anyMatch(r ->
                r.status() == PaymentResourceAvailabilityStatus.UNAVAILABLE)) {
            return PaymentResourceAvailabilityStatus.UNAVAILABLE;
        }

        if (results.stream().anyMatch(r ->
                r.status() == PaymentResourceAvailabilityStatus.INSUFFICIENT)) {
            return PaymentResourceAvailabilityStatus.INSUFFICIENT;
        }

        if (results.stream().allMatch(r ->
                r.status() == PaymentResourceAvailabilityStatus.AVAILABLE)) {
            return PaymentResourceAvailabilityStatus.AVAILABLE;
        }

        return PaymentResourceAvailabilityStatus.NOT_REQUIRED;
    }


    private void assertEditable(MvtTrOperation operation) {
        if (!operation.isEditable()) {
            throw new IllegalStateException(
                    "Only operations with status X / En cours can be modified. Current status: "
                            + operation.getStatus()
            );
        }
    }
}