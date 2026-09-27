package com.checkout.payment.gateway.model;

import com.checkout.payment.gateway.enums.PaymentStatus;

public class RejectedPaymentResponse {

  private final PaymentStatus status = PaymentStatus.REJECTED;
  private final String message;

  public RejectedPaymentResponse(String message) {
    this.message = message;
  }

  public PaymentStatus getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }
}
