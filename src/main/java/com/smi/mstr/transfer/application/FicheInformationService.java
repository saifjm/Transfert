package com.smi.mstr.transfer.application;

import com.smi.mstr.transfer.application.context.WorkflowCommandContext;
import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.domain.entity.TrSupportReglementaire;

public interface FicheInformationService {

    TrSupportReglementaire generateFor(
            MvtTrOperation operation,
            WorkflowCommandContext context
    );
}
