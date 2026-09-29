package com.checkout.payment.gateway.model;

import com.checkout.payment.gateway.enums.PaymentStatus;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public abstract class BasePaymentResponse {
  private UUID id;
  private PaymentStatus status;
  @Schema(example = "4321", description = "Last 4 digits of the card number")
  private String cardNumberLastFour;
  @Schema(example = "4", description = "Expiry month")
  private int expiryMonth;
  @Schema(example = "2030", description = "Expiry year")
  private int expiryYear;
  @Schema(example = "GBP", description = "Currency")
  private String currency;
  @Schema(example = "1050", description = "Amount in minor currency units")
  private int amount;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public PaymentStatus getStatus() {
    return status;
  }

  public void setStatus(PaymentStatus status) {
    this.status = status;
  }

  public String getCardNumberLastFour() {
    return cardNumberLastFour;
  }

  public void setCardNumberLastFour(String cardNumberLastFour) {
    this.cardNumberLastFour = cardNumberLastFour;
  }

  public int getExpiryMonth() {
    return expiryMonth;
  }

  public void setExpiryMonth(int expiryMonth) {
    this.expiryMonth = expiryMonth;
  }

  public int getExpiryYear() {
    return expiryYear;
  }

  public void setExpiryYear(int expiryYear) {
    this.expiryYear = expiryYear;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public int getAmount() {
    return amount;
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() +
        "{id=" + id +
        ", status=" + status +
        ", cardNumberLastFour=" + cardNumberLastFour +
        ", expiryMonth=" + expiryMonth +
        ", expiryYear=" + expiryYear +
        ", currency='" + currency + '\'' +
        ", amount=" + amount +
        '}';
  }
}
