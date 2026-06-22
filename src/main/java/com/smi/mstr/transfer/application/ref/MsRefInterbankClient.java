package com.smi.mstr.transfer.application.ref;

public interface MsRefInterbankClient {

    RefInterbankChainResponse getDefaultInterbankChain(
            RefInterbankChainRequest request
    );
}