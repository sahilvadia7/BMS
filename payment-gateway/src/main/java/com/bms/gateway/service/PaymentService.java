package com.bms.gateway.service;

import com.bms.gateway.dto.request.PaymentRequest;
import com.bms.gateway.dto.response.PaymentResponse;
import org.apache.coyote.BadRequestException;

public interface PaymentService {

	PaymentResponse processExternalTransfer(PaymentRequest request);
}
