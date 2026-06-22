package com.smi.mstr.transfer.api;

import com.smi.mstr.transfer.application.TransferOperationQueryService;
import com.smi.mstr.transfer.application.TransferOperationService;
import com.smi.mstr.transfer.application.context.TransferCreationContext;
import com.smi.mstr.transfer.domain.enums.OriginChannel;
import com.smi.mstr.transfer.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ms-tr/operations")
@RequiredArgsConstructor
public class TransferOperationController {

    private static final String DEFAULT_SOURCE_MODULE = "MS-TR";
    private static final String DEFAULT_USER_ROLE = "AGENT_SAISIE";

    private final TransferOperationService operationService;
    private final TransferOperationQueryService queryService;

    @PostMapping
    public TransferOperationResponse createManualOrder(
            @Valid @RequestBody CreateTransferOrderRequest request,

            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Role-Code", required = false) String roleCode,
            @RequestHeader(value = "X-Branch-Code", required = false) String branchCode,

            @RequestHeader(value = "X-Source-Channel", required = false) String sourceChannel,
            @RequestHeader(value = "X-Source-Module", required = false) String sourceModule,
            @RequestHeader(value = "X-Source-Reference", required = false) String sourceReference,

            @RequestHeader(value = "X-WF-Instance-Id", required = false) String workflowInstanceId,
            @RequestHeader(value = "X-WF-Task-Id", required = false) String workflowTaskId,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId
    ) {
        TransferCreationContext context = new TransferCreationContext(
                defaultIfBlank(userId, "SYSTEM"),
                defaultIfBlank(roleCode, DEFAULT_USER_ROLE),
                requireBranchCode(branchCode),

                resolveSourceChannel(sourceChannel),
                defaultIfBlank(sourceModule, DEFAULT_SOURCE_MODULE),
                sourceReference,

                workflowInstanceId,
                workflowTaskId,
                null,

                correlationId
        );

        return operationService.createManualOrder(request, context);
    }

    @PutMapping("/{operationRef}/draft")
    public TransferOperationResponse saveDraft(
            @PathVariable("operationRef") String operationRef,
            @Valid @RequestBody SaveTransferDraftRequest request
    ) {
        return operationService.saveDraft(operationRef, request);
    }

    @GetMapping("/in-progress")
    public List<TransferOperationListItem> findInProgressOrders(
            @RequestParam(required = false) String branchCode,
            @RequestParam(required = false) String codeAgence
    ) {
        return queryService.findInProgressOrders(
                resolveCodeAgence(branchCode, codeAgence)
        );
    }

    @PutMapping("/{operationRef}/debtor")
    public TransferOperationResponse updateDebtor(
            @PathVariable("operationRef") String operationRef,
            @Valid @RequestBody UpdateDebtorRequest request
    ) {
        return operationService.updateDebtor(operationRef, request);
    }

    @PutMapping("/{operationRef}/creditor")
    public TransferOperationResponse updateCreditor(
            @PathVariable("operationRef") String operationRef,
            @Valid @RequestBody UpdateCreditorRequest request
    ) {
        return operationService.updateCreditor(operationRef, request);
    }

    @PutMapping("/{operationRef}/qualification")
    public TransferOperationResponse updateQualification(
            @PathVariable("operationRef") String operationRef,
            @Valid @RequestBody UpdateTransferQualificationRequest request
    ) {
        return operationService.updateQualification(operationRef, request);
    }

    @PostMapping("/{operationRef}/toilette-control")
    public TransferValidationReport runToiletteControl(
            @PathVariable("operationRef") String operationRef,
            @Valid @RequestBody RunToiletteControlRequest request
    ) {
        return operationService.runToiletteControl(operationRef, request);
    }

    @GetMapping("/{operationRef}/validation-errors")
    public List<ValidationErrorDto> getValidationErrors(
            @PathVariable("operationRef") String operationRef
    ) {
        return operationService.getValidationErrors(operationRef);
    }

    private String resolveCodeAgence(String branchCode, String codeAgence) {
        if (notBlank(codeAgence)) {
            return codeAgence;
        }

        return branchCode;
    }

    private OriginChannel resolveSourceChannel(String sourceChannel) {
        if (sourceChannel == null || sourceChannel.isBlank()) {
            return OriginChannel.AGENCY;
        }

        try {
            return OriginChannel.valueOf(sourceChannel.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid X-Source-Channel: " + sourceChannel
            );
        }
    }

    private String requireBranchCode(String branchCode) {
        if (branchCode == null || branchCode.isBlank()) {
            throw new IllegalArgumentException("X-Branch-Code header is required.");
        }

        return branchCode.trim();
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return notBlank(value) ? value.trim() : defaultValue;
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}