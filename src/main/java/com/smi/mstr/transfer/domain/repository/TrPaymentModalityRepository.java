package com.smi.mstr.transfer.domain.repository;

import com.smi.mstr.transfer.domain.entity.TrPaymentModality;
import com.smi.mstr.transfer.domain.enums.BlockingStatus;
import com.smi.mstr.transfer.domain.enums.PaymentImpactStatus;
import com.smi.mstr.transfer.domain.enums.PaymentModalityStatus;
import com.smi.mstr.transfer.domain.enums.PaymentModalityType;
import com.smi.mstr.transfer.domain.enums.PaymentResourceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TrPaymentModalityRepository extends JpaRepository<TrPaymentModality, Long> {

    List<TrPaymentModality> findByOperationRefOperationOrderBySequenceNoAsc(
            Long refOperation
    );

    Optional<TrPaymentModality> findFirstByOperationRefOperationAndSequenceNo(
            Long refOperation,
            Integer sequenceNo
    );

    List<TrPaymentModality> findByOperationRefOperationAndModalityTypeOrderBySequenceNoAsc(
            Long refOperation,
            PaymentModalityType modalityType
    );

    List<TrPaymentModality> findByOperationRefOperationAndModalityTypeInOrderBySequenceNoAsc(
            Long refOperation,
            Collection<PaymentModalityType> modalityTypes
    );

    List<TrPaymentModality> findByOperationRefOperationAndResourceTypeOrderBySequenceNoAsc(
            Long refOperation,
            PaymentResourceType resourceType
    );

    List<TrPaymentModality> findByOperationRefOperationAndBlockingStatusOrderBySequenceNoAsc(
            Long refOperation,
            BlockingStatus blockingStatus
    );

    List<TrPaymentModality> findByOperationRefOperationAndBlockingStatusInOrderBySequenceNoAsc(
            Long refOperation,
            Collection<BlockingStatus> blockingStatuses
    );

    List<TrPaymentModality> findByOperationRefOperationAndImpactStatusOrderBySequenceNoAsc(
            Long refOperation,
            PaymentImpactStatus impactStatus
    );

    List<TrPaymentModality> findByOperationRefOperationAndModalityStatusOrderBySequenceNoAsc(
            Long refOperation,
            PaymentModalityStatus modalityStatus
    );

    List<TrPaymentModality> findByOperationRefOperationAndModalityStatusInOrderBySequenceNoAsc(
            Long refOperation,
            Collection<PaymentModalityStatus> modalityStatuses
    );

    List<TrPaymentModality> findByDebitAccountNumberOrderByIdPaymentModalityDesc(
            String debitAccountNumber
    );

    List<TrPaymentModality> findByResourceReferenceOrderByIdPaymentModalityDesc(
            String resourceReference
    );

    List<TrPaymentModality> findByBlockingReferenceOrderByIdPaymentModalityDesc(
            String blockingReference
    );

    List<TrPaymentModality> findByImpactReferenceOrderByIdPaymentModalityDesc(
            String impactReference
    );

    boolean existsByOperationRefOperationAndModalityType(
            Long refOperation,
            PaymentModalityType modalityType
    );

    void deleteByOperationRefOperation(
            Long refOperation
    );

    long countByOperationRefOperation(
            Long refOperation
    );
}