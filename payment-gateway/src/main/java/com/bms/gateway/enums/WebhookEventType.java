package com.bms.gateway.enums;

public enum WebhookEventType {

	// -----------------------------
	// PAYMENT EVENTS
	// -----------------------------
	PAYMENT_CREATED,
	PAYMENT_INITIATED,
	PAYMENT_PENDING,
	PAYMENT_PROCESSING,
	PAYMENT_SUCCESS,
	PAYMENT_FAILED,
	PAYMENT_CANCELLED,
	PAYMENT_EXPIRED,
	PAYMENT_CAPTURED,
	PAYMENT_AUTHORIZED,
	PAYMENT_DECLINED,
	PAYMENT_REFUNDED,

	// -----------------------------
	// REFUND EVENTS
	// -----------------------------
	REFUND_INITIATED,
	REFUND_PROCESSING,
	REFUND_SUCCESS,
	REFUND_FAILED,


	BANK_TRANSFER_FAILED,


	// -----------------------------
	// SETTLEMENT EVENTS
	// -----------------------------
	SETTLEMENT_INITIATED,
	SETTLEMENT_COMPLETED,
	SETTLEMENT_FAILED,

	// -----------------------------
	// PROVIDER / INTERNAL SYSTEM EVENTS
	// -----------------------------
	PROVIDER_CALLBACK_RECEIVED,
	PROVIDER_ERROR,
	UNKNOWN, SIGNATURE_VERIFICATION_FAILED
}

