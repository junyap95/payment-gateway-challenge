package com.checkout.payment.gateway.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.checkout.payment.gateway.model.PostPaymentRequest;
import java.time.YearMonth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaymentRequestValidatorTest {

  private PaymentRequestValidator validator;

  @BeforeEach
  void setUp() {
    validator = new PaymentRequestValidator();
  }

  @Test
  void validRequestReturnsNull() {
    assertNull(validator.validate(validRequest()));
  }

  @Test
  void rejectsShortCardNumber() {
    PostPaymentRequest request = validRequest();
    request.setCardNumber("1234567890123");
    assertEquals("Card number must be between 14 and 19 characters",
        validator.validate(request));
  }

  @Test
  void rejectsNonNumericCardNumber() {
    PostPaymentRequest request = validRequest();
    request.setCardNumber("22224053432488ab");
    assertEquals("Card number must only contain numeric characters",
        validator.validate(request));
  }

  @Test
  void rejectsInvalidExpiryMonth() {
    PostPaymentRequest request = validRequest();
    request.setExpiryMonth(13);
    assertEquals("Expiry month must be between 1 and 12", validator.validate(request));
  }

  @Test
  void rejectsExpiredCard() {
    PostPaymentRequest request = validRequest();
    YearMonth past = YearMonth.now().minusMonths(1);
    request.setExpiryMonth(past.getMonthValue());
    request.setExpiryYear(past.getYear());
    assertEquals("Card expiry date must be in the future", validator.validate(request));
  }

  @Test
  void rejectsUnsupportedCurrency() {
    PostPaymentRequest request = validRequest();
    request.setCurrency("JPY");
    assertEquals("Currency is not supported", validator.validate(request));
  }

  @Test
  void rejectsNonPositiveAmount() {
    PostPaymentRequest request = validRequest();
    request.setAmount(0);
    assertEquals("Amount must be a positive integer", validator.validate(request));
  }

  @Test
  void rejectsInvalidCvv() {
    PostPaymentRequest request = validRequest();
    request.setCvv("12");
    assertEquals("CVV must be 3-4 characters long", validator.validate(request));
  }

  private PostPaymentRequest validRequest() {
    PostPaymentRequest request = new PostPaymentRequest();
    request.setCardNumber("2222405343248877");
    request.setExpiryMonth(4);
    request.setExpiryYear(YearMonth.now().plusYears(1).getYear());
    request.setCurrency("GBP");
    request.setAmount(100);
    request.setCvv("123");
    return request;
  }
}
