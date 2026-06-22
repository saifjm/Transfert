package com.smi.mstr.transfer.domain.repository;

import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.domain.enums.TransferOperationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MvtTrOperationRepository extends JpaRepository<MvtTrOperation, Long> {

    /**
     * Recherche par référence métier de l'ordre.
     *
     * Ancien équivalent :
     * operationRef
     *
     * Nouveau champ :
     * REF_ORDRE
     */
    Optional<MvtTrOperation> findByRefOrdre(String refOrdre);


    boolean existsByRefOrdre(String refOrdre);

    /**
     * Recherche par numéro de dossier.
     */
    Optional<MvtTrOperation> findByNumDossier(String numDossier);

    /**
     * Recherche par clé technique BD.
     *
     * REF_OPERATION = clé primaire technique.
     */
    Optional<MvtTrOperation> findByRefOperation(Long refOperation);

    /**
     * Liste des opérations par statut.
     */
    List<MvtTrOperation> findByStatusOrderByCreatedAtDesc(
            TransferOperationStatus status
    );

    /**
     * Ancien branchCode -> nouveau codeAgence.
     */
    List<MvtTrOperation> findByCodeAgenceAndStatusOrderByCreatedAtDesc(
            String codeAgence,
            TransferOperationStatus status
    );

    /**
     * Vue détail opération.
     *
     * Dans le nouveau modèle, les parties principales sont portées par :
     * ULTIMATE_DEBTOR_ID, DEBTOR_ID, CREDITOR_ID, ULTIMATE_CREDITOR_ID.
     *
     * Donc on ne charge plus :
     * parties, postalAddresses, identifications, accounts, financialAgents
     * sauf si vous décidez ensuite de les remapper comme relations JPA.
     */
    @Query("""
            select o
            from MvtTrOperation o
            where o.refOrdre = :refOrdre
            """)
    Optional<MvtTrOperation> findDetailedByRefOrdre(
            @Param("refOrdre") String refOrdre
    );

    /**
     * Vue PB-15 : résultat modalités / disponibilité / sécurisation.
     *
     * Important :
     * Ne pas fetcher paymentModalities + paymentModalities.securities ensemble
     * si les deux sont des List, sinon Hibernate lève MultipleBagFetchException.
     *
     * On fetch seulement paymentModalities.
     * Les securities seront chargées lazy dans un service @Transactional.
     */
    @EntityGraph(attributePaths = {
            "paymentModalities"
    })
    @Query("""
        select o
        from MvtTrOperation o
        where o.refOrdre = :refOrdre
        """)
    Optional<MvtTrOperation> findPaymentReviewByRefOrdre(
            @Param("refOrdre") String refOrdre
    );

    /**
     * Variante par REF_OPERATION technique, utile pour traitements internes.
     */
    @EntityGraph(attributePaths = {
            "paymentModalities"
    })
    @Query("""
            select o
            from MvtTrOperation o
            where o.refOperation = :refOperation
            """)
    Optional<MvtTrOperation> findPaymentReviewByRefOperation(
            @Param("refOperation") Long refOperation
    );


    @Deprecated
    default Optional<MvtTrOperation> findPaymentReviewByOperationRef(String operationRef) {
        return findPaymentReviewByRefOrdre(operationRef);
    }




    // ---------------------------------------------------------------------
    // Compatibility layer temporaire
    // ---------------------------------------------------------------------
    // Ces méthodes permettent de garder temporairement les anciens services
    // qui appellent encore operationRef.
    // Elles délèguent maintenant vers refOrdre.
    // À supprimer une fois tous les services refactorés.
    // ---------------------------------------------------------------------

    @Deprecated
    default Optional<MvtTrOperation> findByOperationRef(String operationRef) {
        return findByRefOrdre(operationRef);
    }

    @Deprecated
    default boolean existsByOperationRef(String operationRef) {
        return existsByRefOrdre(operationRef);
    }

    @Deprecated
    default List<MvtTrOperation> findByBranchCodeAndStatusOrderByCreatedAtDesc(
            String branchCode,
            TransferOperationStatus status
    ) {
        return findByCodeAgenceAndStatusOrderByCreatedAtDesc(branchCode, status);
    }

    @Deprecated
    default Optional<MvtTrOperation> findDetailedByOperationRef(String operationRef) {
        return findDetailedByRefOrdre(operationRef);
    }


}