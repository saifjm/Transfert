package com.smi.mstr.transfer.dto.interbank;

public record FetchDefaultInterbankChainRequest(
        String requestedBy,
        Boolean forceRefresh
) {
}