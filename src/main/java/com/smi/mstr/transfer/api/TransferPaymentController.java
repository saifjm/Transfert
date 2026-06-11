package com.smi.mstr.transfer.api;

import com.smi.mstr.transfer.application.TransferPaymentResourceAvailabilityService;
import com.smi.mstr.transfer.application.TransferPaymentSecurityService;
import com.smi.mstr.transfer.application.TransferPaymentService;
import com.smi.mstr.transfer.dto.payment.CheckPaymentResourceAvailabilityRequest;
import com.smi.mstr.transfer.dto.payment.PaymentModalityDto;
import com.smi.mstr.transfer.dto.payment.PaymentResourceAvailabilityReport;
import com.smi.mstr.transfer.dto.payment.PaymentSecurityItemDto;
import com.smi.mstr.transfer.dto.payment.PaymentSecurityReport;
import com.smi.mstr.transfer.dto.payment.SecurePaymentRequest;
import com.smi.mstr.transfer.dto.payment.UpdatePaymentModalitiesRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ms-tr/operations/{operationRef}/payment")
@RequiredArgsConstructor
public class TransferPaymentController {

    private final TransferPaymentService transferPaymentService;
    private final TransferPaymentResourceAvailabilityService resourceAvailabilityService;
    private final TransferPaymentSecurityService paymentSecurityService;

    /**
     * PB-11 — Saisie des modalités de paiement.
     */
    @PutMapping("/modalities")
    public List<PaymentModalityDto> updatePaymentModalities(
            @PathVariable String operationRef,
            @Valid @RequestBody UpdatePaymentModalitiesRequest request
    ) {
        return transferPaymentService.updatePaymentModalities(operationRef, request);
    }

    /**
     * PB-12 — Vérification de disponibilité de la ressource de financement.
     */
    @PostMapping("/resource-availability")
    public PaymentResourceAvailabilityReport checkPaymentResourceAvailability(
            @PathVariable String operationRef,
            @Valid @RequestBody CheckPaymentResourceAvailabilityRequest request
    ) {
        return resourceAvailabilityService.checkAvailability(operationRef, request);
    }

    /**
     * PB-13 — Sécurisation de la ressource de financement.
     */
    @PostMapping("/security")
    public PaymentSecurityReport securePaymentResources(
            @PathVariable String operationRef,
            @Valid @RequestBody SecurePaymentRequest request
    ) {
        return paymentSecurityService.securePayment(operationRef, request);
    }

    /**
     * PB-14 — Consultation de l'état de sécurisation.
     */
    @GetMapping("/security")
    public List<PaymentSecurityItemDto> getPaymentSecurityStatus(
            @PathVariable String operationRef
    ) {
        return paymentSecurityService.getPaymentSecurityStatus(operationRef);
    }
}