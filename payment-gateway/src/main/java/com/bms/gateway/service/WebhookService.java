package com.bms.gateway.service;

public interface WebhookService {

	void receiveWebhook(String payload, String signature);
	void processPendingWebhooks();
}
