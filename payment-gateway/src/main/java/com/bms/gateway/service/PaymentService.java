package com.bms.gateway.service;

import com.bms.gateway.dto.request.PaymentRequest;
import com.bms.gateway.dto.response.PaymentResponse;


public interface PaymentService {

	PaymentResponse processExternalTransfer(PaymentRequest request);
}
