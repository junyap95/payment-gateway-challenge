package com.checkout.payment.gateway.exception;

import com.checkout.payment.gateway.model.ErrorResponse;
import com.checkout.payment.gateway.model.RejectedPaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CommonExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(CommonExceptionHandler.class);

  @ExceptionHandler(EventProcessingException.class)
  public ResponseEntity<ErrorResponse> handleException(EventProcessingException ex) {
    LOG.info("Payment not found: {}", ex.getMessage());
    return new ResponseEntity<>(new ErrorResponse("Payment record not found"),
        HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(RejectedPaymentException.class)
  public ResponseEntity<RejectedPaymentResponse> handleRejectedPayment(
      RejectedPaymentException ex) {
    LOG.info("Payment rejected: {}", ex.getMessage());
    return new ResponseEntity<>(new RejectedPaymentResponse(ex.getMessage()),
        HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(BankUnavailableException.class)
  public ResponseEntity<ErrorResponse> handleBankUnavailable(BankUnavailableException ex) {
    return new ResponseEntity<>(new ErrorResponse(ex.getMessage()),
        HttpStatus.BAD_GATEWAY);
  }
}
