package com.smi.mstr.transfer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mock.payment-blocking")
public class MockPaymentBlockingProperties {

    private boolean enabled = true;

    private String failOnReferenceContaining = "FAIL";

    private String partialOnReferenceContaining = "PARTIAL";

    private String insufficientOnReferenceContaining = "LOW";

    private long defaultProcessingDelayMs = 0L;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getFailOnReferenceContaining() {
        return failOnReferenceContaining;
    }

    public void setFailOnReferenceContaining(String failOnReferenceContaining) {
        this.failOnReferenceContaining = failOnReferenceContaining;
    }

    public String getPartialOnReferenceContaining() {
        return partialOnReferenceContaining;
    }

    public void setPartialOnReferenceContaining(String partialOnReferenceContaining) {
        this.partialOnReferenceContaining = partialOnReferenceContaining;
    }

    public String getInsufficientOnReferenceContaining() {
        return insufficientOnReferenceContaining;
    }

    public void setInsufficientOnReferenceContaining(String insufficientOnReferenceContaining) {
        this.insufficientOnReferenceContaining = insufficientOnReferenceContaining;
    }

    public long getDefaultProcessingDelayMs() {
        return defaultProcessingDelayMs;
    }

    public void setDefaultProcessingDelayMs(long defaultProcessingDelayMs) {
        this.defaultProcessingDelayMs = defaultProcessingDelayMs;
    }
}