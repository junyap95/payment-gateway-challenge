package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.client.AcquiringBankClient;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.BankUnavailableException;
import com.checkout.payment.gateway.exception.EventProcessingException;
import com.checkout.payment.gateway.exception.RejectedPaymentException;
import com.checkout.payment.gateway.model.BankPaymentRequest;
import com.checkout.payment.gateway.model.BankPaymentResponse;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import com.checkout.payment.gateway.validation.PaymentRequestValidator;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentGatewayService {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentGatewayService.class);

  private final PaymentsRepository paymentsRepository;
  private final PaymentRequestValidator paymentRequestValidator;
  private final AcquiringBankClient acquiringBankClient;

  public PaymentGatewayService(
      PaymentsRepository paymentsRepository,
      PaymentRequestValidator paymentRequestValidator,
      AcquiringBankClient acquiringBankClient) {
    this.paymentsRepository = paymentsRepository;
    this.paymentRequestValidator = paymentRequestValidator;
    this.acquiringBankClient = acquiringBankClient;
  }

  public PostPaymentResponse getPaymentById(UUID id) {
    LOG.debug("Requesting access to payment with ID {}", id);
    return paymentsRepository.get(id)
        .orElseThrow(() -> new EventProcessingException("Invalid ID"));
  }

  public PostPaymentResponse processPayment(PostPaymentRequest paymentRequest) {
    String validationError = paymentRequestValidator.validate(paymentRequest);
    if (validationError != null) {
      LOG.info("Rejecting payment request: {}", validationError);
      throw new RejectedPaymentException(validationError);
    }

    BankPaymentRequest bankPaymentRequest = new BankPaymentRequest(paymentRequest);
    BankPaymentResponse bankResponse =
        acquiringBankClient.processPayment(bankPaymentRequest);

    // defensive check for when a 200 bankResponse has no body
    if (bankResponse == null) {
      throw new BankUnavailableException("Invalid response from acquiring bank");
    }

    PaymentStatus status =
        bankResponse.isAuthorized() ? PaymentStatus.AUTHORIZED : PaymentStatus.DECLINED;

    PostPaymentResponse payment = new PostPaymentResponse();
    payment.setId(UUID.randomUUID());
    payment.setStatus(status);
    payment.setCardNumberLastFour(paymentRequest.getCardNumberLastFour());
    payment.setExpiryMonth(paymentRequest.getExpiryMonth());
    payment.setExpiryYear(paymentRequest.getExpiryYear());
    payment.setCurrency(paymentRequest.getCurrency());
    payment.setAmount(paymentRequest.getAmount());

    paymentsRepository.add(payment);
    LOG.info("Processed payment {} with status {}", payment.getId(), status.getName());
    return payment;
  }
}
