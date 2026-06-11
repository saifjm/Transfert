package com.smi.mstr.transfer.application.payment;

import com.smi.mstr.transfer.dto.payment.PaymentSecurityItemDto;

public interface PaymentSecurityClient {

    PaymentSecurityItemDto secure(PaymentSecurityCommand command);
}
