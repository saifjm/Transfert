package com.smi.mstr.transfer.application;

import com.smi.mstr.transfer.application.mapper.PaymentModalityMapper;
import com.smi.mstr.transfer.application.payment.PaymentModalityValidator;
import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.domain.entity.TrPaymentModality;
import com.smi.mstr.transfer.domain.enums.OperationEventType;
import com.smi.mstr.transfer.domain.enums.TransferOperationStatus;
import com.smi.mstr.transfer.domain.repository.MvtTrOperationRepository;
import com.smi.mstr.transfer.dto.payment.PaymentModalityDto;
import com.smi.mstr.transfer.dto.payment.UpdatePaymentModalitiesRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferPaymentService {

    private static final String AGENT_SAISIE_ROLE = "AGENT_SAISIE";

    private final MvtTrOperationRepository operationRepository;
    private final PaymentModalityMapper paymentModalityMapper;
    private final PaymentModalityValidator paymentModalityValidator;
    private final TransferOperationEventService eventService;

    @Transactional
    public List<PaymentModalityDto> updatePaymentModalities(
            String operationRef,
            UpdatePaymentModalitiesRequest request
    ) {
        MvtTrOperation operation = findOperationByRef(operationRef);
        assertEditable(operation);

        paymentModalityValidator.validateDtoList(request.modalities());

        operation.clearPaymentModalities();

        int sequence = 1;

        for (PaymentModalityDto dto : request.modalities()) {
            PaymentModalityDto enrichedDto = enrichTargetAmountAndCurrency(operation, dto);

            TrPaymentModality modality =
                    paymentModalityMapper.toEntity(enrichedDto, sequence++);

            operation.addPaymentModality(modality);

            paymentModalityValidator.validateEntity(operation, modality);
        }

        operation.setUpdatedAt(LocalDateTime.now());

        MvtTrOperation saved = operationRepository.save(operation);

        eventService.registerEvent(
                saved,
                OperationEventType.PAYMENT_MODALITIES_UPDATED,
                TransferOperationStatus.X,
                TransferOperationStatus.X,
                request.updatedBy(),
                AGENT_SAISIE_ROLE,
                request.comment(),
                null
        );

        return saved.getPaymentModalities()
                .stream()
                .map(paymentModalityMapper::toDto)
                .toList();
    }




    private PaymentModalityDto enrichTargetAmountAndCurrency(
            MvtTrOperation operation,
            PaymentModalityDto dto
    ) {
        BigDecimal targetAmount = dto.targetAmount() != null
                ? dto.targetAmount()
                : calculateTargetAmount(operation, dto.sharePercent());

        String targetCurrency = dto.targetCurrency() != null
                ? dto.targetCurrency()
                : operation.getTransferCurrency();

        return new PaymentModalityDto(
                dto.modalityId(),
                dto.modalityType(),
                dto.sequenceNo(),
                dto.sharePercent(),

                dto.sourceAmount(),
                dto.sourceCurrency(),

                targetAmount,
                targetCurrency,

                dto.debitAccountRef(),
                dto.debitAccountCurrency(),

                dto.fxMode(),
                dto.fxRate(),
                dto.fxDealRef(),
                dto.forwardContractRef(),

                dto.financingRef(),
                dto.receivedFundsRef(),
                dto.interbankCoverRef(),
                dto.counterpartyBankBic(),
                dto.valueDate(),

                dto.availabilityStatus(),
                dto.availableAmount(),
                dto.availableCurrency(),
                dto.availabilityCheckedAt(),
                dto.availabilityMessage(),

                dto.securityStatus()
        );
    }

    private BigDecimal calculateTargetAmount(
            MvtTrOperation operation,
            BigDecimal sharePercent
    ) {
        if (operation.getTransferAmount() == null) {
            throw new IllegalStateException(
                    "Transfer amount must be filled before payment modalities."
            );
        }

        return operation.getTransferAmount()
                .multiply(sharePercent)
                .divide(new BigDecimal("100.00"), 3, RoundingMode.HALF_UP);
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