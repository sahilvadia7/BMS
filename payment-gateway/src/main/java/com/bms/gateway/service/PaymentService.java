package com.bms.gateway.service;

import com.bms.gateway.dto.request.PaymentRequest;
import com.bms.gateway.dto.response.PaymentResponse;
import org.apache.coyote.BadRequestException;

public interface PaymentService {

	PaymentResponse initiatePayment(PaymentRequest request) throws BadRequestException;
	PaymentResponse processPayment(String paymentId);
	void confirmPayment(String providerPaymentId, boolean isSuccess, String rawPayload);
	PaymentResponse getPayment(String paymentId);
	PaymentResponse cancelPayment(String paymentId) throws BadRequestException;
	PaymentResponse retryPayment(String paymentId) throws BadRequestException;
	void handlePaymentWebhook(String rawPayload);

}
