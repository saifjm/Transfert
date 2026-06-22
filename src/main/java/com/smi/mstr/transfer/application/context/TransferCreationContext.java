package com.smi.mstr.transfer.application.context;

import com.smi.mstr.transfer.domain.enums.OriginChannel;

public record TransferCreationContext(
        String connectedUserId,
        String connectedUserRole,
        String branchCode,

        OriginChannel sourceChannel,
        String sourceModule,
        String sourceReference,

        String workflowInstanceId,
        String workflowTaskId,
        String workflowContextJson,

        String correlationId
) {
}