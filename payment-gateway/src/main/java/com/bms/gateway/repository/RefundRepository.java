package com.bms.gateway.repository;

import com.bms.gateway.dto.response.RefundResponse;
import com.bms.gateway.model.Refund;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RefundRepository extends JpaRepository<Refund,String> {

	RefundResponse findByPaymentId(String paymentId);
}
