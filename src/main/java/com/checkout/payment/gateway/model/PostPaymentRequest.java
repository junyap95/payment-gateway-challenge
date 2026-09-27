package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

public class PostPaymentRequest implements Serializable {
  
  @JsonProperty("card_number")
  @Schema(example = "2222405343248877", description = "14-19 digits")
  private String cardNumber;
  @JsonProperty("expiry_month")
  @Schema(example = "4", description = "1-12")
  private Integer expiryMonth;
  @JsonProperty("expiry_year")
  @Schema(example = "2030")
  private Integer expiryYear;
  @Schema(example = "GBP", allowableValues = {"GBP", "USD", "EUR"})
  private String currency;
  @Schema(example = "1050", description = "Minor currency units, e.g. 1050 = 10.50")
  private Integer amount;
  @Schema(example = "123", description = "3-4 digits")
  private String cvv;

  public String getCardNumber() {
    return cardNumber;
  }

  public void setCardNumber(String cardNumber) {
    this.cardNumber = cardNumber;
  }

  public Integer getExpiryMonth() {
    return expiryMonth;
  }

  public void setExpiryMonth(Integer expiryMonth) {
    this.expiryMonth = expiryMonth;
  }

  public Integer getExpiryYear() {
    return expiryYear;
  }

  public void setExpiryYear(Integer expiryYear) {
    this.expiryYear = expiryYear;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public Integer getAmount() {
    return amount;
  }

  public void setAmount(Integer amount) {
    this.amount = amount;
  }

  public String getCvv() {
    return cvv;
  }

  public void setCvv(String cvv) {
    this.cvv = cvv;
  }

  @JsonIgnore
  public String getExpiryDateForBank() {
    return String.format("%02d/%d", expiryMonth, expiryYear);
  }

  @JsonIgnore
  public String getCardNumberLastFour() {
      if (cardNumber == null || cardNumber.length() < 4) {
          return "N/A"; 
      }
      return cardNumber.substring(cardNumber.length() - 4);
  }
  
  @Override
  public String toString() {
      return "PostPaymentRequest{" +
              "cardNumberLastFour='" + getCardNumberLastFour() + '\'' + 
              ", expiryMonth=" + expiryMonth +
              ", expiryYear=" + expiryYear +
              ", currency='" + currency + '\'' +
              ", amount=" + amount +
              '}';
  }
}
