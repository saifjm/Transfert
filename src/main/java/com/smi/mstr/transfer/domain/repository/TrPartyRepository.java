package com.smi.mstr.transfer.domain.repository;

import com.smi.mstr.transfer.domain.entity.TrParty;
import com.smi.mstr.transfer.domain.enums.PartyRole;
import com.smi.mstr.transfer.domain.enums.PartyType;
import com.smi.mstr.transfer.domain.enums.ResidencyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TrPartyRepository extends JpaRepository<TrParty, Long> {

    List<TrParty> findByOperationRefOperationOrderByPartyRoleAscSequenceNoAsc(
            Long refOperation
    );

    List<TrParty> findByOperationRefOperationAndPartyRoleOrderBySequenceNoAsc(
            Long refOperation,
            PartyRole partyRole
    );

    Optional<TrParty> findFirstByOperationRefOperationAndPartyRoleOrderBySequenceNoAsc(
            Long refOperation,
            PartyRole partyRole
    );

    List<TrParty> findByOperationRefOperationAndPartyRoleInOrderByPartyRoleAscSequenceNoAsc(
            Long refOperation,
            Collection<PartyRole> partyRoles
    );

    List<TrParty> findByOperationRefOperationAndPartyTypeOrderBySequenceNoAsc(
            Long refOperation,
            PartyType partyType
    );

    List<TrParty> findByOperationRefOperationAndPartyTypeInOrderByPartyRoleAscSequenceNoAsc(
            Long refOperation,
            Collection<PartyType> partyTypes
    );

    List<TrParty> findByCustomerIdOrderByIdPartyDesc(
            Long customerId
    );

    List<TrParty> findByExternalPartyRefOrderByIdPartyDesc(
            String externalPartyRef
    );

    List<TrParty> findByBicOrderByIdPartyDesc(
            String bic
    );

    List<TrParty> findByBankCodeOrderByIdPartyDesc(
            String bankCode
    );

    Optional<TrParty> findFirstByAccountIbanOrderByIdPartyDesc(
            String accountIban
    );

    Optional<TrParty> findFirstByAccountNumberOrderByIdPartyDesc(
            String accountNumber
    );

    List<TrParty> findByCountryCodeOrderByIdPartyDesc(
            String countryCode
    );

    List<TrParty> findByResidencyStatusOrderByIdPartyDesc(
            ResidencyStatus residencyStatus
    );

    boolean existsByOperationRefOperationAndPartyRole(
            Long refOperation,
            PartyRole partyRole
    );

    void deleteByOperationRefOperationAndPartyRole(
            Long refOperation,
            PartyRole partyRole
    );

    void deleteByOperationRefOperationAndPartyRoleIn(
            Long refOperation,
            Collection<PartyRole> partyRoles
    );

    long countByOperationRefOperation(
            Long refOperation
    );
}