package com.smi.mstr.transfer.api;

import com.smi.mstr.transfer.application.TransferInterbankEnrichmentService;
import com.smi.mstr.transfer.dto.interbank.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ms-tr/operations/{operationRef}/interbank-enrichment")
@RequiredArgsConstructor
public class TransferInterbankEnrichmentController {

    private final TransferInterbankEnrichmentService service;

    @GetMapping
    public InterbankEnrichmentResponse getEnrichment(
            @PathVariable String operationRef
    ) {
        return service.getEnrichment(operationRef);
    }

    @PostMapping("/default-chain")
    public InterbankEnrichmentResponse fetchDefaultChain(
            @PathVariable String operationRef,
            @RequestBody FetchDefaultInterbankChainRequest request
    ) {
        return service.fetchDefaultChain(operationRef, request);
    }

    @PutMapping("/payment-path")
    public InterbankEnrichmentResponse savePaymentPath(
            @PathVariable String operationRef,
            @RequestBody SavePaymentPathRequest request
    ) {
        return service.savePaymentPath(operationRef, request);
    }

    @PutMapping("/instructions")
    public InterbankEnrichmentResponse saveInstructions(
            @PathVariable String operationRef,
            @RequestBody SaveInterbankInstructionsRequest request
    ) {
        return service.saveInstructions(operationRef, request);
    }

    @PostMapping("/cover")
    public InterbankEnrichmentResponse determineCover(
            @PathVariable String operationRef,
            @RequestBody DetermineCoverRequest request
    ) {
        return service.determineCover(operationRef, request);
    }

    @PostMapping("/control")
    public InterbankControlReport controlCorrespondents(
            @PathVariable String operationRef
    ) {
        return service.controlCorrespondents(operationRef);
    }
}