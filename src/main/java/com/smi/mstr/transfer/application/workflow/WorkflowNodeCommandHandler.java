package com.smi.mstr.transfer.application.workflow;

import com.smi.mstr.transfer.application.context.WorkflowCommandContext;
import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.domain.enums.WorkflowNodeCode;
import com.smi.mstr.transfer.dto.workflow.WorkflowTransferCommandRequest;

public interface WorkflowNodeCommandHandler {

    boolean supports(WorkflowNodeCode nodeCode);

    default MvtTrOperation create(
            WorkflowTransferCommandRequest request,
            WorkflowCommandContext context
    ) {
        throw new UnsupportedOperationException("Create not supported by this node.");
    }

    default void apply(
            MvtTrOperation operation,
            WorkflowTransferCommandRequest request,
            WorkflowCommandContext context
    ) {
        throw new UnsupportedOperationException("Apply not supported by this node.");
    }
}