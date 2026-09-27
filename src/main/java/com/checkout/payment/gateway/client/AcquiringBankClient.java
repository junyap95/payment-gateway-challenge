package com.checkout.payment.gateway.client;

import com.checkout.payment.gateway.exception.BankUnavailableException;
import com.checkout.payment.gateway.model.BankPaymentRequest;
import com.checkout.payment.gateway.model.BankPaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class AcquiringBankClient {

  private static final Logger LOG = LoggerFactory.getLogger(AcquiringBankClient.class);

  private final RestTemplate restTemplate;
  private final String bankPaymentsUrl;

  public AcquiringBankClient(
      RestTemplate restTemplate,
      @Value("${bank.simulator.url}") String bankSimulatorUrl) {
    this.restTemplate = restTemplate;
    this.bankPaymentsUrl = bankSimulatorUrl + "/payments";
  }

  public BankPaymentResponse processPayment(BankPaymentRequest request) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<BankPaymentRequest> entity = new HttpEntity<>(request, headers);

    try {
      ResponseEntity<BankPaymentResponse> response =
          restTemplate.postForEntity(bankPaymentsUrl, entity, BankPaymentResponse.class);
      return response.getBody();
    } catch (HttpStatusCodeException ex) {
      LOG.error("Acquiring bank returned status {}", ex.getStatusCode().value());
      String message = ex.getStatusCode().is4xxClientError()
          ? "Payment could not be processed by the acquiring bank"
          : "Acquiring bank is unavailable";
      throw new BankUnavailableException(message);
    } catch (RestClientException ex) {
      LOG.error("Failed to call acquiring bank", ex);
      throw new BankUnavailableException("Failed to call acquiring bank");
    }
  }
}
