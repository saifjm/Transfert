package com.smi.mstr.transfer.application;

import com.smi.mstr.transfer.application.payment.PaymentSecurityCalculationService;
import com.smi.mstr.transfer.application.payment.PaymentSecurityClient;
import com.smi.mstr.transfer.application.payment.PaymentSecurityCommand;
import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.domain.entity.TrPaymentModality;
import com.smi.mstr.transfer.domain.entity.TrPaymentSecurity;
import com.smi.mstr.transfer.domain.enums.OperationEventType;
import com.smi.mstr.transfer.domain.enums.PaymentResourceAvailabilityStatus;
import com.smi.mstr.transfer.domain.enums.PaymentSecurityStatus;
import com.smi.mstr.transfer.domain.enums.TransferOperationStatus;
import com.smi.mstr.transfer.domain.repository.MvtTrOperationRepository;
import com.smi.mstr.transfer.dto.payment.PaymentSecurityItemDto;
import com.smi.mstr.transfer.dto.payment.PaymentSecurityReport;
import com.smi.mstr.transfer.dto.payment.SecurePaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferPaymentSecurityService {

    private static final String AGENT_SAISIE_ROLE = "AGENT_SAISIE";

    private final MvtTrOperationRepository operationRepository;
    private final PaymentSecurityCalculationService calculationService;
    private final PaymentSecurityClient securityClient;
    private final TransferOperationEventService eventService;

    @Transactional
    public PaymentSecurityReport securePayment(
            String operationRef,
            SecurePaymentRequest request
    ) {
        MvtTrOperation operation = findOperationByRef(operationRef);
        assertEditable(operation);

        LocalDateTime securedAt = LocalDateTime.now();

        List<PaymentSecurityItemDto> results = operation.getPaymentModalities()
                .stream()
                .map(modality -> secureOne(
                        modality,
                        request.estimatedFeesAmount(),
                        request.estimatedFeesCurrency()
                ))
                .toList();

        PaymentSecurityStatus overallStatus = resolveOverallStatus(results);

        operation.setUpdatedAt(LocalDateTime.now());
        operationRepository.save(operation);

        eventService.registerEvent(
                operation,
                OperationEventType.PAYMENT_SECURITY_APPLIED,
                TransferOperationStatus.X,
                TransferOperationStatus.X,
                request.securedBy(),
                AGENT_SAISIE_ROLE,
                "Payment resources secured: " + overallStatus,
                null
        );

        return new PaymentSecurityReport(
                operationRef,
                overallStatus,
                securedAt,
                results
        );
    }

    @Transactional(readOnly = true)
    public List<PaymentSecurityItemDto> getPaymentSecurityStatus(String operationRef) {
        MvtTrOperation operation = findOperationByRef(operationRef);

        return operation.getPaymentModalities()
                .stream()
                .flatMap(modality -> modality.getSecurities().stream())
                .map(this::toDto)
                .toList();
    }

    private PaymentSecurityItemDto secureOne(
            TrPaymentModality modality,
            java.math.BigDecimal estimatedFeesAmount,
            String estimatedFeesCurrency
    ) {
        if (modality.getAvailabilityStatus() != PaymentResourceAvailabilityStatus.AVAILABLE) {
            modality.setSecurityStatus(PaymentSecurityStatus.REQUIRED_NOT_SECURED);

            return new PaymentSecurityItemDto(
                    null,
                    modality.getModalityId(),
                    null,
                    PaymentSecurityStatus.REQUIRED_NOT_SECURED,
                    null,
                    modality.getTargetAmount(),
                    modality.getTargetCurrency(),
                    modality.getFxRate(),
                    null,
                    null,
                    estimatedFeesAmount,
                    estimatedFeesCurrency,
                    null,
                    null,
                    null,
                    null,
                    "Payment resource must be available before security."
            );
        }

        modality.clearSecurities();

        PaymentSecurityCommand command = calculationService.buildSecurityCommand(
                modality,
                estimatedFeesAmount,
                estimatedFeesCurrency
        );

        PaymentSecurityItemDto result = securityClient.secure(command);

        TrPaymentSecurity security = TrPaymentSecurity.builder()
                .operation(modality.getOperation())
                .modality(modality)
                .resourceType(result.resourceType())
                .securityStatus(result.securityStatus())
                .resourceRef(result.resourceRef())
                .requestedAmount(result.requestedAmount())
                .requestedCurrency(result.requestedCurrency())
                .fxRate(result.fxRate())
                .counterValueAmount(result.counterValueAmount())
                .counterValueCurrency(result.counterValueCurrency())
                .estimatedFeesAmount(result.estimatedFeesAmount())
                .estimatedFeesCurrency(result.estimatedFeesCurrency())
                .securedAmount(result.securedAmount())
                .securedCurrency(result.securedCurrency())
                .securityReference(result.securityReference())
                .securedAt(result.securedAt())
                .securityMessage(result.message())
                .build();

        modality.addSecurity(security);
        modality.setSecurityStatus(result.securityStatus());

        return result;
    }

    private PaymentSecurityItemDto toDto(TrPaymentSecurity security) {
        return new PaymentSecurityItemDto(
                security.getSecurityId(),
                security.getModality().getModalityId(),
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

    private PaymentSecurityStatus resolveOverallStatus(List<PaymentSecurityItemDto> results) {
        if (results.isEmpty()) {
            return PaymentSecurityStatus.NOT_REQUIRED;
        }

        if (results.stream().anyMatch(r -> r.securityStatus() == PaymentSecurityStatus.FAILED)) {
            return PaymentSecurityStatus.FAILED;
        }

        if (results.stream().anyMatch(r -> r.securityStatus() == PaymentSecurityStatus.REQUIRED_NOT_SECURED)) {
            return PaymentSecurityStatus.REQUIRED_NOT_SECURED;
        }

        if (results.stream().allMatch(r -> r.securityStatus() == PaymentSecurityStatus.SECURED)) {
            return PaymentSecurityStatus.SECURED;
        }

        return PaymentSecurityStatus.NOT_REQUIRED;
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