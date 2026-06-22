package com.smi.mstr.transfer.application.ref;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class MsRefRoutingInterbankClient implements MsRefInterbankClient {

    private static final String DEFAULT_CHAIN_ENDPOINT =
            "/api/ms-ref/interbank/default-chain";

    private final MsRefClientProperties properties;

    @Override
    public RefInterbankChainResponse getDefaultInterbankChain(
            RefInterbankChainRequest request
    ) {
        String baseUrl = properties.selectedBaseUrl();

        RestClient client = RestClient.builder()
                .baseUrl(baseUrl)
                .build();

        return client.post()
                .uri(DEFAULT_CHAIN_ENDPOINT)
                .body(request)
                .retrieve()
                .body(RefInterbankChainResponse.class);
    }
}