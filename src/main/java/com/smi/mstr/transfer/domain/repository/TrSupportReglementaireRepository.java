package com.smi.mstr.transfer.domain.repository;

import com.smi.mstr.transfer.domain.entity.TrSupportReglementaire;
import com.smi.mstr.transfer.domain.enums.StatutImputationSupport;
import com.smi.mstr.transfer.domain.enums.StatutReservationSupport;
import com.smi.mstr.transfer.domain.enums.StatutSupportReglementaire;
import com.smi.mstr.transfer.domain.enums.StatutValidationSupport;
import com.smi.mstr.transfer.domain.enums.TypeSupportReglementaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TrSupportReglementaireRepository extends JpaRepository<TrSupportReglementaire, Long> {

    List<TrSupportReglementaire> findByOperationRefOperationOrderBySequenceNoAsc(
            Long refOperation
    );

    Optional<TrSupportReglementaire> findFirstByOperationRefOperationAndSequenceNo(
            Long refOperation,
            Integer sequenceNo
    );

    List<TrSupportReglementaire> findByOperationRefOperationAndTypeSupportOrderBySequenceNoAsc(
            Long refOperation,
            TypeSupportReglementaire typeSupport
    );

    Optional<TrSupportReglementaire> findFirstByOperationRefOperationAndTypeSupportOrderBySequenceNoAsc(
            Long refOperation,
            TypeSupportReglementaire typeSupport
    );

    List<TrSupportReglementaire> findByOperationRefOperationAndTypeSupportInOrderBySequenceNoAsc(
            Long refOperation,
            Collection<TypeSupportReglementaire> typeSupports
    );

    Optional<TrSupportReglementaire> findFirstByTypeSupportAndNumSupportOrderByIdSupportDesc(
            TypeSupportReglementaire typeSupport,
            String numSupport
    );

    List<TrSupportReglementaire> findByTypeSupportAndNumSupportOrderByIdSupportDesc(
            TypeSupportReglementaire typeSupport,
            String numSupport
    );

    List<TrSupportReglementaire> findByNumSupportOrderByIdSupportDesc(
            String numSupport
    );

    List<TrSupportReglementaire> findByNumIdentificationOrderByIdSupportDesc(
            String numIdentification
    );

    List<TrSupportReglementaire> findByCodeBanqueOrderByIdSupportDesc(
            String codeBanque
    );

    List<TrSupportReglementaire> findByOperationRefOperationAndStatutSupportOrderBySequenceNoAsc(
            Long refOperation,
            StatutSupportReglementaire statutSupport
    );

    List<TrSupportReglementaire> findByOperationRefOperationAndStatutValidationOrderBySequenceNoAsc(
            Long refOperation,
            StatutValidationSupport statutValidation
    );

    List<TrSupportReglementaire> findByOperationRefOperationAndStatutReservationOrderBySequenceNoAsc(
            Long refOperation,
            StatutReservationSupport statutReservation
    );

    List<TrSupportReglementaire> findByOperationRefOperationAndStatutImputationOrderBySequenceNoAsc(
            Long refOperation,
            StatutImputationSupport statutImputation
    );

    boolean existsByOperationRefOperationAndTypeSupport(
            Long refOperation,
            TypeSupportReglementaire typeSupport
    );

    boolean existsByTypeSupportAndNumSupport(
            TypeSupportReglementaire typeSupport,
            String numSupport
    );

    void deleteByOperationRefOperation(
            Long refOperation
    );

    long countByOperationRefOperation(
            Long refOperation
    );
}