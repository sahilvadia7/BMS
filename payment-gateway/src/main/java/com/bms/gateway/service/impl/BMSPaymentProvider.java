package com.bms.gateway.service.impl;

import com.bms.gateway.dto.response.ProviderInitResponse;
import com.bms.gateway.dto.response.ProviderRetryResponse;
import com.bms.gateway.enums.ProviderPaymentStatus;
import com.bms.gateway.model.Payment;
import com.bms.gateway.service.PaymentProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class BMSPaymentProvider implements PaymentProvider {

	@Override
	public ProviderInitResponse initiate(Payment payment) {

		log.info("[DummyProvider] Initiating payment {}", payment.getId());

		String providerRef = "DUMMY-PAY-" + UUID.randomUUID();

		return ProviderInitResponse.builder()
				.providerReference(providerRef)
				.rawResponse("{ \"status\": \"INITIATED\", \"providerRef\": \"" + providerRef + "\" }")
				.build();
	}

	@Override
	public ProviderPaymentStatus checkStatus(Payment payment) {

		log.info("[DummyProvider] Checking status for {}", payment.getId());

		// Simulate random provider behavior
		int random = (int) (Math.random() * 5);

		return switch (random) {
			case 0 -> ProviderPaymentStatus.SUCCESS;
			case 1 -> ProviderPaymentStatus.FAILED;
			case 2 -> ProviderPaymentStatus.PENDING;
			case 3 -> ProviderPaymentStatus.PROCESSING;
			default -> ProviderPaymentStatus.AUTHORIZED;
		};
	}

	@Override
	public ProviderRetryResponse retry(Payment payment) {

		log.info("[DummyProvider] Retrying payment {}", payment.getId());

		String newRef = "DUMMY-RETRY-" + UUID.randomUUID();

		return ProviderRetryResponse.builder()
				.providerReference(newRef)
				.rawResponse("{ \"retry\": true, \"newRef\": \"" + newRef + "\" }")
				.build();
	}

	@Override
	public void cancel(Payment payment) {
		log.info("[DummyProvider] Cancelling payment {}", payment.getId());

	}

	@Override
	public boolean verifyWebhookSignature(String rawPayload, String signature) {
		return true;
	}
}

