package com.smi.mstr.transfer.dto.workflow;

import com.smi.mstr.transfer.domain.enums.TransferType;
import com.smi.mstr.transfer.dto.workflow.sections.*;

import java.time.LocalDate;

public record WorkflowTransferCommandRequest(

        /*
         * Null for creation nodes.
         * Required for back-office / validation / enrichment nodes.
         */
        String operationRef,

        TransferType transferType,

        /*
         * Transfer header entered by the actor or received from channel.
         */
        TransferInstructionSection transferInstruction,

        /*
         * Parties, accounts and agents represented through TrParty.
         */
        TransferPartiesSection parties,

        /*
         * Payment / financing modalities.
         */
        PaymentModalitiesSection paymentModalities,

        /*
         * Regulatory supports: TCE, fiche information, BCT authorization...
         */
        RegulatorySupportsSection regulatorySupports,

        /*
         * Interbank data selected by back-office.
         */
        InterbankDataSection interbankData,

        /*
         * Validation action.
         */
        ValidationDecisionSection validationDecision
) {
}