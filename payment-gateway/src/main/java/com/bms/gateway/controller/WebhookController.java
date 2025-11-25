package com.bms.gateway.controller;

import com.bms.gateway.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/webhooks")
public class WebhookController {

	private final WebhookService webhookService;


	@PostMapping("/receive")
	public ResponseEntity<String> receiveWebhook(
			@RequestBody String payload,
			@RequestHeader(value = "X-Signature", required = false) String signature
	) {

		log.info("Webhook received → signature={}, payload={}", signature, payload);

		webhookService.receiveWebhook(payload, signature);

		return ResponseEntity.ok("Webhook Received");
	}


	@PostMapping("/process")
	public ResponseEntity<String> processPending() {

		log.info("Manual webhook reprocessing triggered");

		webhookService.processPendingWebhooks();

		return ResponseEntity.ok("Pending webhooks processed");
	}
}

