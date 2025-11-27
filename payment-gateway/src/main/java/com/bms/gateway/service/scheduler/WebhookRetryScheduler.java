package com.bms.gateway.service.scheduler;

import com.bms.gateway.service.WebhookService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WebhookRetryScheduler {

	private final WebhookService webhookService;

	public WebhookRetryScheduler(WebhookService webhookService) {
		this.webhookService = webhookService;
	}

	@Scheduled(fixedDelay = 120000)
	public void retryPendingWebhooks() {
		log.info(" → Retrying pending webhooks...");
		webhookService.processPendingWebhooks();
	}
}