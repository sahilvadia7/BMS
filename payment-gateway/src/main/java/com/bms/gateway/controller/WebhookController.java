package com.bms.gateway.controller;

import com.bms.gateway.service.WebhookService;

import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@Tag(name = "Webhook Management", description = "Endpoints for webhook handling")
public class WebhookController {

	private final WebhookService webhookService;

	public WebhookController(WebhookService webhookService) {
		this.webhookService = webhookService;
	}

	@Operation(summary = "Receive webhook", description = "Access: Public (Payment Provider)")
	@PostMapping("/receive")
	public ResponseEntity<String> receiveWebhook(
			@RequestBody String payload,
			@RequestHeader(value = "X-Signature", required = false) String signature) {

		log.info("Webhook received → signature={}, payload={}", signature, payload);

		webhookService.receiveWebhook(payload, signature);

		return ResponseEntity.ok("Webhook Received");
	}

	@Operation(summary = "Process pending webhooks", description = "Access: Admin, System")
	@PostMapping("/process")
	public ResponseEntity<String> processPending() {

		log.info("Manual webhook reprocessing triggered");

		webhookService.processPendingWebhooks();

		return ResponseEntity.ok("Pending webhooks processed");
	}
}
