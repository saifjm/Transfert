package com.smi.mstr.transfer.application.mapper;

import com.smi.mstr.transfer.domain.entity.TrPaymentModality;
import com.smi.mstr.transfer.domain.enums.PaymentResourceAvailabilityStatus;
import com.smi.mstr.transfer.domain.enums.PaymentSecurityStatus;
import com.smi.mstr.transfer.dto.payment.PaymentModalityDto;
import org.springframework.stereotype.Component;

@Component
public class PaymentModalityMapper {

    public TrPaymentModality toEntity(PaymentModalityDto dto, int defaultSequenceNo) {
        return TrPaymentModality.builder()
                .modalityId(dto.modalityId())
                .modalityType(dto.modalityType())
                .sequenceNo(dto.sequenceNo() != null ? dto.sequenceNo() : defaultSequenceNo)

                .sourceAmount(dto.sourceAmount())
                .sourceCurrency(dto.sourceCurrency())
                .targetAmount(dto.targetAmount())
                .targetCurrency(dto.targetCurrency())

                .debitAccountRef(dto.debitAccountRef())
                .debitAccountCurrency(dto.debitAccountCurrency())

                .fxMode(dto.fxMode())
                .fxRate(dto.fxRate())
                .fxDealRef(dto.fxDealRef())
                .forwardContractRef(dto.forwardContractRef())

                .financingRef(dto.financingRef())
                .receivedFundsRef(dto.receivedFundsRef())
                .interbankCoverRef(dto.interbankCoverRef())
                .counterpartyBankBic(dto.counterpartyBankBic())
                .valueDate(dto.valueDate())

                .availabilityStatus(PaymentResourceAvailabilityStatus.NOT_REQUIRED)
                .securityStatus(PaymentSecurityStatus.NOT_REQUIRED)
                .build();
    }

    public PaymentModalityDto toDto(TrPaymentModality entity) {
        return new PaymentModalityDto(
                entity.getModalityId(),
                entity.getModalityType(),
                entity.getSequenceNo(),

                entity.getSourceAmount(),
                entity.getSourceCurrency(),
                entity.getTargetAmount(),
                entity.getTargetCurrency(),

                entity.getDebitAccountRef(),
                entity.getDebitAccountCurrency(),

                entity.getFxMode(),
                entity.getFxRate(),
                entity.getFxDealRef(),
                entity.getForwardContractRef(),

                entity.getFinancingRef(),
                entity.getReceivedFundsRef(),
                entity.getInterbankCoverRef(),
                entity.getCounterpartyBankBic(),
                entity.getValueDate(),

                entity.getAvailabilityStatus(),
                entity.getAvailableAmount(),
                entity.getAvailableCurrency(),
                entity.getAvailabilityCheckedAt(),
                entity.getAvailabilityMessage(),

                entity.getSecurityStatus()
        );
    }
}