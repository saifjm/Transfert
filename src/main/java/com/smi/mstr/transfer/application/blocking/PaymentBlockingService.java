package com.smi.mstr.transfer.application.blocking;

public interface PaymentBlockingService {

    PaymentBlockingResponse block(PaymentBlockingRequest request);
}
