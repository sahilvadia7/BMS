package com.bms.gateway.service;

import com.bms.gateway.dto.request.RefundRequest;
import com.bms.gateway.dto.response.RefundResponse;
import com.bms.gateway.enums.RefundStatus;

public interface RefundService {

	RefundResponse initiateRefund(RefundRequest request);
	RefundResponse updateRefundStatus(String refundId, RefundStatus newStatus);
	RefundResponse getRefundsByPayment(String paymentId);
	void handleRefundWebhook(String payload);
}
