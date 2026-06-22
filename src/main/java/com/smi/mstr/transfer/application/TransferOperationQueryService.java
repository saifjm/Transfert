package com.smi.mstr.transfer.application;

import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.domain.enums.TransferOperationStatus;
import com.smi.mstr.transfer.domain.repository.MvtTrOperationRepository;
import com.smi.mstr.transfer.dto.TransferOperationListItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferOperationQueryService {

    private final MvtTrOperationRepository operationRepository;

    /**
     * Liste des opérations en cours.
     *
     * Le paramètre branchCode est conservé côté API pour compatibilité,
     * mais il correspond maintenant au champ CODE_AGENCE.
     */
    @Transactional(readOnly = true)
    public List<TransferOperationListItem> findInProgressOrders(String branchCode) {
        List<MvtTrOperation> operations;

        if (branchCode == null || branchCode.isBlank()) {
            operations = operationRepository.findByStatusOrderByCreatedAtDesc(
                    TransferOperationStatus.X
            );
        } else {
            operations = operationRepository.findByCodeAgenceAndStatusOrderByCreatedAtDesc(
                    branchCode,
                    TransferOperationStatus.X
            );
        }

        return operations.stream()
                .map(this::toListItem)
                .toList();
    }

    private TransferOperationListItem toListItem(MvtTrOperation op) {
        return new TransferOperationListItem(
                op.getRefOperation(),

                /*
                 * API compatibility:
                 * operationRef côté frontend/Postman = REF_ORDRE côté DB.
                 */
                op.getRefOrdre(),

                op.getStatus(),

                /*
                 * completionStatus supprimé du nouveau modèle.
                 * On ne le retourne plus.
                 */
                op.getTypeTransfert(),

                op.getSwiftPriority(),

                /*
                 * branchCode côté API = CODE_AGENCE côté DB.
                 */
                op.getCodeAgence(),

                op.getNumDossier(),
                op.getDateOperation(),
                op.getDateDossier(),

                op.getMntOrdre(),
                op.getCodeDeviseOrdre(),

                op.getMntDevise(),
                op.getCodeDevise(),

                op.getDateValeurTransfert(),

                op.getSourceChannel(),
                op.getSourceModule(),
                op.getSourceReference(),

                op.getCreatedAt(),
                op.getDateValidation()
        );
    }
}