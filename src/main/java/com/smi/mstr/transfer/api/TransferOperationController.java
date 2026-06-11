package com.smi.mstr.transfer.api;

import com.smi.mstr.transfer.application.TransferOperationQueryService;
import com.smi.mstr.transfer.application.TransferOperationService;
import com.smi.mstr.transfer.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ms-tr/operations")
@RequiredArgsConstructor
public class TransferOperationController {

    private final TransferOperationService operationService;
    private final TransferOperationQueryService queryService;

    @PostMapping
    public TransferOperationResponse createManualOrder(
            @Valid @RequestBody CreateTransferOrderRequest request
    ) {
        return operationService.createManualOrder(request);
    }

    @PutMapping("/{operationRef}/draft")
    public TransferOperationResponse saveDraft(
            @PathVariable String operationRef,
            @Valid @RequestBody SaveTransferDraftRequest request
    ) {
        return operationService.saveDraft(operationRef, request);
    }

    @GetMapping("/in-progress")
    public List<TransferOperationListItem> findInProgressOrders(
            @RequestParam(required = false) String branchCode
    ) {
        return queryService.findInProgressOrders(branchCode);
    }

    @PutMapping("/{operationRef}/debtor")
    public TransferOperationResponse updateDebtor(
            @PathVariable String operationRef,
            @Valid @RequestBody UpdateDebtorRequest request
    ) {
        return operationService.updateDebtor(operationRef, request);
    }

    @PutMapping("/{operationRef}/creditor")
    public TransferOperationResponse updateCreditor(
            @PathVariable String operationRef,
            @Valid @RequestBody UpdateCreditorRequest request
    ) {
        return operationService.updateCreditor(operationRef, request);
    }

    @PutMapping("/{operationRef}/qualification")
    public TransferOperationResponse updateQualification(
            @PathVariable String operationRef,
            @Valid @RequestBody UpdateTransferQualificationRequest request
    ) {
        return operationService.updateQualification(operationRef, request);
    }

    @PostMapping("/{operationRef}/toilette-control")
    public TransferValidationReport runToiletteControl(
            @PathVariable String operationRef,
            @Valid @RequestBody RunToiletteControlRequest request
    ) {
        return operationService.runToiletteControl(operationRef, request);
    }

    @GetMapping("/{operationRef}/validation-errors")
    public List<ValidationErrorDto> getValidationErrors(
            @PathVariable String operationRef
    ) {
        return operationService.getValidationErrors(operationRef);
    }




}