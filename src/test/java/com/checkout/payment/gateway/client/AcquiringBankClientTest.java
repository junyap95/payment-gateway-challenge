package com.checkout.payment.gateway.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.checkout.payment.gateway.exception.BankUnavailableException;
import com.checkout.payment.gateway.model.BankPaymentRequest;
import com.checkout.payment.gateway.model.BankPaymentResponse;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class AcquiringBankClientTest {

  private MockRestServiceServer bank;
  private AcquiringBankClient client;

  @BeforeEach
  void setUp() {
    RestTemplate restTemplate = new RestTemplate();
    bank = MockRestServiceServer.bindTo(restTemplate).build();
    client = new AcquiringBankClient(restTemplate, "http://bank");
  }

  @Test
  void sendsBankFieldsWithZeroPaddedExpiryDate() {
    bank.expect(requestTo("http://bank/payments"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(jsonPath("$.card_number").value("2222405343248877"))
        .andExpect(jsonPath("$.expiry_date").value("04/2030"))
        .andExpect(jsonPath("$.currency").value("GBP"))
        .andExpect(jsonPath("$.amount").value(100))
        .andExpect(jsonPath("$.cvv").value("123"))
        .andRespond(withSuccess("""
            {"authorized": true, "authorization_code": "0bb07405-6d44-4b50-a14f-7ae0beff13ad"}
            """, MediaType.APPLICATION_JSON));

    BankPaymentResponse response = client.processPayment(bankRequest());

    assertTrue(response.isAuthorized());
    bank.verify();
  }

  @Test
  void bankClientErrorIsReportedAsNotProcessed() {
    bank.expect(requestTo("http://bank/payments")).andRespond(withBadRequest());

    BankUnavailableException ex =
        assertThrows(BankUnavailableException.class, () -> client.processPayment(bankRequest()));

    assertEquals("Payment could not be processed by the acquiring bank", ex.getMessage());
  }

  @Test
  void bankServerErrorIsReportedAsUnavailable() {
    bank.expect(requestTo("http://bank/payments"))
        .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

    BankUnavailableException ex =
        assertThrows(BankUnavailableException.class, () -> client.processPayment(bankRequest()));

    assertEquals("Acquiring bank is unavailable", ex.getMessage());
  }

  private BankPaymentRequest bankRequest() {
    PostPaymentRequest request = new PostPaymentRequest();
    request.setCardNumber("2222405343248877");
    request.setExpiryMonth(4);
    request.setExpiryYear(2030);
    request.setCurrency("GBP");
    request.setAmount(100);
    request.setCvv("123");
    return new BankPaymentRequest(request);
  }
}
