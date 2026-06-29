package com.smi.mstr.transfer.domain.repository;

import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.domain.enums.OriginChannel;
import com.smi.mstr.transfer.domain.enums.TransferOperationStatus;
import com.smi.mstr.transfer.domain.enums.TransferType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MvtTrOperationRepository extends JpaRepository<MvtTrOperation, Long> {

    Optional<MvtTrOperation> findByRefOperation(Long refOperation);

    Optional<MvtTrOperation> findByRefOrdre(String refOrdre);

    Optional<MvtTrOperation> findByUetr(String uetr);

    Optional<MvtTrOperation> findByTransactionId(String transactionId);

    Optional<MvtTrOperation> findByEndToEndId(String endToEndId);

    Optional<MvtTrOperation> findByCorrelationId(String correlationId);

    boolean existsByRefOrdre(String refOrdre);

    boolean existsByUetr(String uetr);

    boolean existsByTransactionId(String transactionId);

    boolean existsByEndToEndId(String endToEndId);

    List<MvtTrOperation> findByStatusOrderByCreatedAtDesc(
            TransferOperationStatus status
    );

    Page<MvtTrOperation> findByStatusOrderByCreatedAtDesc(
            TransferOperationStatus status,
            Pageable pageable
    );

    List<MvtTrOperation> findByStatusInOrderByCreatedAtDesc(
            Collection<TransferOperationStatus> statuses
    );

    Page<MvtTrOperation> findByStatusInOrderByCreatedAtDesc(
            Collection<TransferOperationStatus> statuses,
            Pageable pageable
    );

    List<MvtTrOperation> findByCodeAgenceAndStatusOrderByCreatedAtDesc(
            String codeAgence,
            TransferOperationStatus status
    );

    Page<MvtTrOperation> findByCodeAgenceAndStatusOrderByCreatedAtDesc(
            String codeAgence,
            TransferOperationStatus status,
            Pageable pageable
    );

    Page<MvtTrOperation> findByCodeAgenceAndStatusInOrderByCreatedAtDesc(
            String codeAgence,
            Collection<TransferOperationStatus> statuses,
            Pageable pageable
    );

    Page<MvtTrOperation> findByTypeTransfertAndStatusOrderByCreatedAtDesc(
            TransferType typeTransfert,
            TransferOperationStatus status,
            Pageable pageable
    );

    Page<MvtTrOperation> findBySourceChannelAndStatusOrderByCreatedAtDesc(
            OriginChannel sourceChannel,
            TransferOperationStatus status,
            Pageable pageable
    );

    List<MvtTrOperation> findByWorkflowInstanceIdOrderByCreatedAtDesc(
            String workflowInstanceId
    );

    Optional<MvtTrOperation> findByWorkflowTaskId(String workflowTaskId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select o
            from MvtTrOperation o
            where o.refOperation = :refOperation
            """)
    Optional<MvtTrOperation> findByRefOperationForUpdate(
            @Param("refOperation") Long refOperation
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select o
            from MvtTrOperation o
            where o.refOrdre = :refOrdre
            """)
    Optional<MvtTrOperation> findByRefOrdreForUpdate(
            @Param("refOrdre") String refOrdre
    );

    @Query("""
            select o
            from MvtTrOperation o
            where (:status is null or o.status = :status)
              and (:typeTransfert is null or o.typeTransfert = :typeTransfert)
              and (:codeAgence is null or o.codeAgence = :codeAgence)
              and (:dateFrom is null or o.dateOperation >= :dateFrom)
              and (:dateTo is null or o.dateOperation <= :dateTo)
            order by o.createdAt desc
            """)
    Page<MvtTrOperation> searchOperations(
            @Param("status") TransferOperationStatus status,
            @Param("typeTransfert") TransferType typeTransfert,
            @Param("codeAgence") String codeAgence,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            Pageable pageable
    );

    @Query("""
            select count(o)
            from MvtTrOperation o
            where o.status = :status
              and o.createdAt >= :createdAfter
            """)
    long countByStatusCreatedAfter(
            @Param("status") TransferOperationStatus status,
            @Param("createdAfter") LocalDateTime createdAfter
    );

    boolean existsByRefOrdreAndRefOperationNot(
            String refOrdre,
            Long refOperation
    );
}
