package com.smi.mstr.transfer.application.payment;

import com.smi.mstr.transfer.dto.payment.PaymentResourceAvailabilityItemDto;

public interface PaymentResourceAvailabilityClient {

    PaymentResourceAvailabilityItemDto check(PaymentResourceCommand command);
}
