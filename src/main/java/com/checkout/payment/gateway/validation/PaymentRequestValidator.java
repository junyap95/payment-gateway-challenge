package com.checkout.payment.gateway.validation;

import com.checkout.payment.gateway.model.PostPaymentRequest;
import java.time.YearMonth;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class PaymentRequestValidator {

  private static final Set<String> SUPPORTED_CURRENCIES = Set.of("GBP", "USD", "EUR");

  public String validate(PostPaymentRequest request) {
    if (request == null) {
      return "Payment request is required";
    }

    String cardNumber = request.getCardNumber();
    if (cardNumber == null || cardNumber.isBlank()) {
      return "Card number is required";
    }
    if (cardNumber.length() < 14 || cardNumber.length() > 19) {
      return "Card number must be between 14 and 19 characters";
    }
    if (!isNumeric(cardNumber)) {
      return "Card number must only contain numeric characters";
    }

    Integer expiryMonth = request.getExpiryMonth();
    if (expiryMonth == null) {
      return "Expiry month is required";
    }
    if (expiryMonth < 1 || expiryMonth > 12) {
      return "Expiry month must be between 1 and 12";
    }

    Integer expiryYear = request.getExpiryYear();
    if (expiryYear == null) {
      return "Expiry year is required";
    }
    if (YearMonth.of(expiryYear, expiryMonth).isBefore(YearMonth.now())) {
      return "Card expiry date must be in the future";
    }

    String currency = request.getCurrency();
    if (currency == null || currency.isBlank()) {
      return "Currency is required";
    }
    if (currency.length() != 3) {
      return "Currency must be 3 characters";
    }
    if (!SUPPORTED_CURRENCIES.contains(currency)) {
      return "Currency is not supported";
    }

    Integer amount = request.getAmount();
    if (amount == null) {
      return "Amount is required";
    }
    if (amount <= 0) {
      return "Amount must be a positive integer";
    }

    String cvv = request.getCvv();
    if (cvv == null || cvv.isBlank()) {
      return "CVV is required";
    }
    if (cvv.length() < 3 || cvv.length() > 4) {
      return "CVV must be 3-4 characters long";
    }
    if (!isNumeric(cvv)) {
      return "CVV must only contain numeric characters";
    }

    return null;
  }

  private boolean isNumeric(String value) {
    if(value == null || value.isBlank()) {
      return false;
    }
    for(char c : value.toCharArray()) {
      if(!Character.isDigit(c)) {
        return false;
      }
    }
    return true;
  }
}
