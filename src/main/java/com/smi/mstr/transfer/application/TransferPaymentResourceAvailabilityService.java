package com.smi.mstr.transfer.application;

import com.smi.mstr.transfer.application.payment.PaymentResourceCommand;
import com.smi.mstr.transfer.application.payment.PaymentResourceAvailabilityClient;
import com.smi.mstr.transfer.application.payment.PaymentSecurityCalculationService;
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
    private final PaymentSecurityCalculationService calculationService;
    private final PaymentResourceAvailabilityClient availabilityClient;
    private final TransferOperationEventService eventService;

    @Transactional
    public PaymentResourceAvailabilityReport checkAvailability(
            String operationRef,
            CheckPaymentResourceAvailabilityRequest request
    ) {
        MvtTrOperation operation = findOperationByRef(operationRef);
        assertEditable(operation);

        LocalDateTime checkedAt = LocalDateTime.now();

        List<PaymentResourceAvailabilityItemDto> results = operation.getPaymentModalities()
                .stream()
                .map(modality -> checkOne(modality, checkedAt))
                .toList();

        PaymentResourceAvailabilityStatus overallStatus = resolveOverallStatus(results);

        operation.setUpdatedAt(LocalDateTime.now());
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
                operationRef,
                overallStatus,
                checkedAt,
                results
        );
    }

    private PaymentResourceAvailabilityItemDto checkOne(
            TrPaymentModality modality,
            LocalDateTime checkedAt
    ) {
        PaymentResourceCommand command = calculationService.buildAvailabilityCommand(modality);

        PaymentResourceAvailabilityItemDto result = availabilityClient.check(command);

        modality.setAvailabilityStatus(result.status());
        modality.setAvailableAmount(result.availableAmount());
        modality.setAvailableCurrency(result.availableCurrency());
        modality.setAvailabilityCheckedAt(checkedAt);
        modality.setAvailabilityMessage(result.message());

        return result;
    }

    private PaymentResourceAvailabilityStatus resolveOverallStatus(
            List<PaymentResourceAvailabilityItemDto> results
    ) {
        if (results.isEmpty()) {
            return PaymentResourceAvailabilityStatus.NOT_REQUIRED;
        }

        if (results.stream().anyMatch(r -> r.status() == PaymentResourceAvailabilityStatus.ERROR)) {
            return PaymentResourceAvailabilityStatus.ERROR;
        }

        if (results.stream().anyMatch(r -> r.status() == PaymentResourceAvailabilityStatus.UNAVAILABLE)) {
            return PaymentResourceAvailabilityStatus.UNAVAILABLE;
        }

        if (results.stream().anyMatch(r -> r.status() == PaymentResourceAvailabilityStatus.INSUFFICIENT)) {
            return PaymentResourceAvailabilityStatus.INSUFFICIENT;
        }

        return PaymentResourceAvailabilityStatus.AVAILABLE;
    }

    private MvtTrOperation findOperationByRef(String operationRef) {
        return operationRepository.findByOperationRef(operationRef)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transfer operation not found: " + operationRef
                ));
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