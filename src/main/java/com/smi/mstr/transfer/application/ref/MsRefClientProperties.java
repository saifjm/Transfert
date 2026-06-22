package com.smi.mstr.transfer.application.ref;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "downstream.ms-ref")
public record MsRefClientProperties(
        MsRefMode mode,
        String realBaseUrl,
        String dummyBaseUrl
) {

    public String selectedBaseUrl() {
        if (mode == MsRefMode.DUMMY) {
            return dummyBaseUrl;
        }

        return realBaseUrl;
    }
}