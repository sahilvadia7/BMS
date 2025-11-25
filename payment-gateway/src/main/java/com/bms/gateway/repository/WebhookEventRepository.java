package com.bms.gateway.repository;

import com.bms.gateway.model.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent,String> {
	List<WebhookEvent> findByProcessedFalse();
}
