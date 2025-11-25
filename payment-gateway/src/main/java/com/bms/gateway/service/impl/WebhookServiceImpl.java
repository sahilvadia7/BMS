package com.bms.gateway.service.impl;

import com.bms.gateway.enums.WebhookEventType;
import com.bms.gateway.model.WebhookEvent;
import com.bms.gateway.repository.WebhookEventRepository;
import com.bms.gateway.service.WebhookService;
import com.bms.gateway.service.PaymentService;
import com.bms.gateway.service.RefundService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebhookServiceImpl implements WebhookService {

	private final WebhookEventRepository webhookEventRepository;
	private final PaymentService paymentService;
	private final RefundService refundService;

	private final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * STEP 1: RECEIVE AND STORE WEBHOOK
	 */
	@Override
	@Transactional
	public void receiveWebhook(String payload, String signature) {

		log.info("Received webhook (signature={})", signature);

		WebhookEvent event = new WebhookEvent();
		event.setPayload(payload);
		event.setSignature(signature);

		try {
			JsonNode json = objectMapper.readTree(payload);
			String type = json.get("eventType").asText();

			WebhookEventType eventType = WebhookEventType.valueOf(type.toUpperCase());
			event.setEventType(eventType);

		} catch (Exception e) {
			log.error("Invalid webhook payload format", e);
			event.setEventType(WebhookEventType.UNKNOWN);
		}

		webhookEventRepository.save(event);

		log.info("Webhook stored with id={}", event.getId());
	}


	/**
	 * STEP 2: PROCESS UNPROCESSED WEBHOOKS (CRON JOB OR INVOKED MANUALLY)
	 */
	@Override
	@Transactional
	public void processPendingWebhooks() {

		var pendingEvents = webhookEventRepository.findByProcessedFalse();

		if (pendingEvents.isEmpty()) {
			log.info("No pending webhook events");
			return;
		}

		log.info("Processing {} webhook events", pendingEvents.size());

		for (WebhookEvent event : pendingEvents) {
			try {
				processWebhookEvent(event);
				event.setProcessed(true);
				webhookEventRepository.save(event);

			} catch (Exception e) {
				log.error("Webhook processing failed for id={} : {}", event.getId(), e.getMessage());
				event.setRetryCount(event.getRetryCount() + 1);


				if (event.getRetryCount() > 5) {
					log.error("Webhook id={} exceeded max retry attempts", event.getId());
					event.setProcessed(true);
				}

				webhookEventRepository.save(event);
			}
		}
	}

	/**
	 * STEP 3: ROUTE WEBHOOK TO SPECIFIC HANDLERS (PAYMENT / REFUND)
	 */
	@Transactional
	protected void processWebhookEvent(WebhookEvent event) throws Exception {

		log.info("Processing webhook id={}, type={}", event.getId(), event.getEventType());

		JsonNode json = objectMapper.readTree(event.getPayload());

		switch (event.getEventType()) {

			case PAYMENT_SUCCESS, PAYMENT_FAILED, PAYMENT_PENDING -> {
				String providerPaymentId = json.get("providerPaymentId").asText();
				boolean isSuccess = json.get("status").asText().equalsIgnoreCase("SUCCESS");

				log.info("Routing payment webhook → providerPaymentId={}", providerPaymentId);

				paymentService.confirmPayment(providerPaymentId, isSuccess, event.getPayload());
			}

			case REFUND_SUCCESS, REFUND_FAILED -> {
				String refundId = json.get("refundId").asText();
				String status = json.get("status").asText();

				log.info("Routing refund webhook → refundId={}", refundId);

				refundService.handleRefundWebhook(event.getPayload());
			}

			default -> {
				log.warn("Unknown webhook type received id={} payload={}",
						event.getId(), event.getPayload());
			}
		}
	}
}
