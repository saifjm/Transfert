package com.smi.mstr.transfer.api;

import com.smi.mstr.transfer.application.WorkflowTransferCommandService;
import com.smi.mstr.transfer.application.context.WorkflowCommandContext;
import com.smi.mstr.transfer.domain.enums.WorkflowNodeCode;
import com.smi.mstr.transfer.dto.TransferOperationResponse;
import com.smi.mstr.transfer.dto.workflow.WorkflowTransferCommandRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ms-tr/operations")
@RequiredArgsConstructor
public class WorkflowTransferCommandController {

    private final WorkflowTransferCommandService service;

    @PostMapping("/workflow-command")
    public TransferOperationResponse submitWorkflowCommand(
            @RequestHeader("X-WF-Instance-Id") String workflowInstanceId,
            @RequestHeader("X-WF-Task-Id") String workflowTaskId,
            @RequestHeader("X-WF-Node-Code") WorkflowNodeCode workflowNodeCode,
            @RequestHeader("X-WF-Actor-Role") String workflowActorRole,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Branch-Code") String branchCode,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody WorkflowTransferCommandRequest request
    ) {
        WorkflowCommandContext context = new WorkflowCommandContext(
                workflowInstanceId,
                workflowTaskId,
                workflowNodeCode,
                workflowActorRole,
                userId,
                branchCode,
                correlationId,
                idempotencyKey
        );

        return service.handle(request, context);
    }
}