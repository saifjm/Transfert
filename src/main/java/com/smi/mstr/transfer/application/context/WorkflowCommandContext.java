package com.smi.mstr.transfer.application.context;

import com.smi.mstr.transfer.domain.enums.WorkflowNodeCode;

public record WorkflowCommandContext(
        String workflowInstanceId,
        String workflowTaskId,
        WorkflowNodeCode workflowNodeCode,
        String workflowActorRole,
        String userId,
        String branchCode,
        String correlationId,
        String idempotencyKey
) {
}
