package com.checkout.payment.gateway.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.checkout.payment.gateway.client.AcquiringBankClient;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.AcquiringBankException;
import com.checkout.payment.gateway.model.BankPaymentRequest;
import com.checkout.payment.gateway.model.BankPaymentResponse;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import com.jayway.jsonpath.JsonPath;

import java.time.YearMonth;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentGatewayControllerTest {

  @Autowired
  private MockMvc mvc;
  @Autowired
  PaymentsRepository paymentsRepository;
  @MockBean
  private AcquiringBankClient acquiringBankClient;

  @BeforeEach void setUp() {
    paymentsRepository.clear();
  }

  @Test
  void whenPaymentWithIdExistThenCorrectPaymentIsReturned() throws Exception {
    PostPaymentResponse payment = new PostPaymentResponse();
    payment.setId(UUID.randomUUID());
    payment.setAmount(10);
    payment.setCurrency("USD");
    payment.setStatus(PaymentStatus.AUTHORIZED);
    payment.setExpiryMonth(12);
    payment.setExpiryYear(2024);
    payment.setCardNumberLastFour("4321");

    paymentsRepository.add(payment);

    mvc.perform(MockMvcRequestBuilders.get("/payment/" + payment.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(payment.getStatus().getName()))
        .andExpect(jsonPath("$.cardNumberLastFour").value(payment.getCardNumberLastFour()))
        .andExpect(jsonPath("$.expiryMonth").value(payment.getExpiryMonth()))
        .andExpect(jsonPath("$.expiryYear").value(payment.getExpiryYear()))
        .andExpect(jsonPath("$.currency").value(payment.getCurrency()))
        .andExpect(jsonPath("$.amount").value(payment.getAmount()));
  }

  @Test
  void whenPaymentWithIdDoesNotExistThen404IsReturned() throws Exception {
    mvc.perform(MockMvcRequestBuilders.get("/payment/" + UUID.randomUUID()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Payment record not found"));
  }

  @Test
  void whenValidPaymentAuthorizedThenPaymentIsStoredAndReturned() throws Exception {
    BankPaymentResponse bankResponse = new BankPaymentResponse();
    bankResponse.setAuthorized(true);
    bankResponse.setAuthorizationCode(UUID.randomUUID().toString());
    when(acquiringBankClient.processPayment(any(BankPaymentRequest.class)))
        .thenReturn(bankResponse);

    int futureYear = YearMonth.now().plusYears(1).getYear();

    MvcResult result = mvc.perform(MockMvcRequestBuilders.post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "card_number": "2222405343248877",
                  "expiry_month": 4,
                  "expiry_year": %d,
                  "currency": "GBP",
                  "amount": 100,
                  "cvv": "123"
                }
                """.formatted(futureYear)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("Authorized"))
        .andExpect(jsonPath("$.cardNumberLastFour").value(8877))
        .andExpect(jsonPath("$.expiryMonth").value(4))
        .andExpect(jsonPath("$.expiryYear").value(futureYear))
        .andExpect(jsonPath("$.currency").value("GBP"))
        .andExpect(jsonPath("$.amount").value(100))
        .andExpect(jsonPath("$.id").exists())
        .andReturn();

    String paymentId = JsonPath.read(
        result.getResponse().getContentAsString(), "$.id");

    mvc.perform(MockMvcRequestBuilders.get("/payment/" + paymentId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("Authorized"))
        .andExpect(jsonPath("$.cardNumberLastFour").value(8877));
  }

  @Test
  void whenValidPaymentDeclinedThenDeclinedStatusIsReturned() throws Exception {
    BankPaymentResponse bankResponse = new BankPaymentResponse();
    bankResponse.setAuthorized(false);
    bankResponse.setAuthorizationCode("");
    when(acquiringBankClient.processPayment(any(BankPaymentRequest.class)))
        .thenReturn(bankResponse);

    int futureYear = YearMonth.now().plusYears(1).getYear();

    MvcResult result = mvc.perform(MockMvcRequestBuilders.post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "card_number": "2222405343248878",
                  "expiry_month": 4,
                  "expiry_year": %d,
                  "currency": "USD",
                  "amount": 100,
                  "cvv": "123"
                }
                """.formatted(futureYear)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("Declined"))
        .andExpect(jsonPath("$.cardNumberLastFour").value(8878))
        .andExpect(jsonPath("$.id").exists()).andReturn();

        String paymentId = JsonPath.read(
          result.getResponse().getContentAsString(), "$.id");

        mvc.perform(MockMvcRequestBuilders.get("/payment/" + paymentId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("Declined"))
        .andExpect(jsonPath("$.cardNumberLastFour").value(8878));
  }

  @Test
  void whenInvalidPaymentThenRejectedWithoutCallingBank() throws Exception {
    mvc.perform(MockMvcRequestBuilders.post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "card_number": "123",
                  "expiry_month": 4,
                  "expiry_year": 2030,
                  "currency": "GBP",
                  "amount": 100,
                  "cvv": "123"
                }
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value("Rejected"))
        .andExpect(jsonPath("$.message").exists());

    verify(acquiringBankClient, never()).processPayment(any());
  }

  @Test
  void whenAmountHasFractionThenRequestIsRejectedWithoutCallingBank() throws Exception {
    mvc.perform(MockMvcRequestBuilders.post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "card_number": "2222405343248877",
                  "expiry_month": 4,
                  "expiry_year": 2030,
                  "currency": "GBP",
                  "amount": 200.10,
                  "cvv": "123"
                }
                """))
        .andExpect(status().isBadRequest());

    verify(acquiringBankClient, never()).processPayment(any());
  }

  @Test
  void whenBankUnavailableThenBadGatewayIsReturned() throws Exception {
    when(acquiringBankClient.processPayment(any(BankPaymentRequest.class)))
        .thenThrow(new AcquiringBankException(
            "Acquiring bank is unavailable"));

    int futureYear = YearMonth.now().plusYears(1).getYear();

    mvc.perform(MockMvcRequestBuilders.post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "card_number": "2222405343248870",
                  "expiry_month": 4,
                  "expiry_year": %d,
                  "currency": "EUR",
                  "amount": 100,
                  "cvv": "123"
                }
                """.formatted(futureYear)))
        .andExpect(status().isBadGateway())
        .andExpect(jsonPath("$.message").value("Acquiring bank is unavailable"));
  }
}
