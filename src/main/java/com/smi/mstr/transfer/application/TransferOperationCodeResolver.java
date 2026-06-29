package com.smi.mstr.transfer.application;

import com.smi.mstr.transfer.application.context.WorkflowCommandContext;
import com.smi.mstr.transfer.config.MsTrOperationCodeProperties;
import com.smi.mstr.transfer.domain.enums.TransferType;
import com.smi.mstr.transfer.domain.enums.WorkflowNodeCode;
import com.smi.mstr.transfer.dto.workflow.WorkflowTransferCommandRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransferOperationCodeResolver {

    private final MsTrOperationCodeProperties properties;

    public Long resolve(
            WorkflowTransferCommandRequest request,
            WorkflowCommandContext context
    ) {
        validate(request, context);

        WorkflowNodeCode nodeCode = context.workflowNodeCode();
        TransferType transferType = request.transferType();

        MsTrOperationCodeProperties.NodeOperationCodes nodeCodes =
                properties.getByNode().get(nodeCode);

        Long resolvedCode = resolveFromNode(nodeCodes, transferType);

        if (resolvedCode != null) {
            return resolvedCode;
        }

        return resolveDefault(transferType);
    }

    private Long resolveFromNode(
            MsTrOperationCodeProperties.NodeOperationCodes nodeCodes,
            TransferType transferType
    ) {
        if (nodeCodes == null) {
            return null;
        }

        return switch (transferType) {
            case C -> nodeCodes.getCommercial();
            case F -> nodeCodes.getFinancial();
        };
    }

    private Long resolveDefault(TransferType transferType) {
        Long defaultCode = switch (transferType) {
            case C -> properties.getDefaultCommercial();
            case F -> properties.getDefaultFinancial();
        };

        if (defaultCode == null) {
            throw new IllegalStateException(
                    "No default codeOperation configured for transfer type: " + transferType
            );
        }

        return defaultCode;
    }

    private void validate(
            WorkflowTransferCommandRequest request,
            WorkflowCommandContext context
    ) {
        if (request == null) {
            throw new IllegalArgumentException("Workflow transfer request is required.");
        }

        if (request.transferType() == null) {
            throw new IllegalArgumentException("transferType is required to resolve codeOperation.");
        }

        if (context == null) {
            throw new IllegalArgumentException("Workflow context is required.");
        }

        if (context.workflowNodeCode() == null) {
            throw new IllegalArgumentException("workflowNodeCode is required to resolve codeOperation.");
        }
    }
}
