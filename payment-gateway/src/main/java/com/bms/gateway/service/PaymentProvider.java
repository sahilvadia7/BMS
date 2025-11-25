package com.bms.gateway.service;


import com.bms.gateway.dto.response.ProviderInitResponse;
import com.bms.gateway.dto.response.ProviderRetryResponse;
import com.bms.gateway.enums.ProviderPaymentStatus;
import com.bms.gateway.model.Payment;

public interface PaymentProvider {

	ProviderInitResponse initiate(Payment payment);

	ProviderPaymentStatus checkStatus(Payment payment);

	ProviderRetryResponse retry(Payment payment);

	void cancel(Payment payment);

	boolean verifyWebhookSignature(String rawPayload, String signature);
}
