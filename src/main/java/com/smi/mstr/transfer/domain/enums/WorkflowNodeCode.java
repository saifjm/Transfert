package com.smi.mstr.transfer.domain.enums;

public enum WorkflowNodeCode {

    AGENCY_INITIATION,
    DIGITAL_CHANNEL_INITIATION,
    PACS008_INITIATION,

    BACK_OFFICE_INTERBANK_SELECTION,
    BACK_OFFICE_VALIDATION,

    COMPLIANCE_REVIEW,
    FINAL_EXECUTION;

    public boolean isCreationNode() {
        return this == AGENCY_INITIATION
                || this == DIGITAL_CHANNEL_INITIATION
                || this == PACS008_INITIATION;
    }

    public boolean isBackOfficeNode() {
        return this == BACK_OFFICE_INTERBANK_SELECTION
                || this == BACK_OFFICE_VALIDATION;
    }
}