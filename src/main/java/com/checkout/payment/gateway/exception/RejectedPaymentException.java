package com.checkout.payment.gateway.exception;

public class RejectedPaymentException extends RuntimeException {

  public RejectedPaymentException(String message) {
    super(message);
  }
}
