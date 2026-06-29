package com.smi.mstr.transfer.api.error;

import com.smi.mstr.transfer.application.blocking.PaymentBlockingException;
import com.smi.mstr.transfer.application.validation.payment.PaymentModalityValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class MsTrExceptionHandler {

    @ExceptionHandler(PaymentModalityValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Object handlePaymentModalityValidationException(
            PaymentModalityValidationException exception
    ) {
        return exception.getReport();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Object handleIllegalArgumentException(
            IllegalArgumentException exception
    ) {
        return new ErrorResponse(
                "BAD_REQUEST",
                exception.getMessage()
        );
    }

    @ExceptionHandler(PaymentBlockingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Object handlePaymentBlockingException(
            PaymentBlockingException exception
    ) {
        return exception.getReport();
    }

    public record ErrorResponse(
            String code,
            String message
    ) {
    }
}
