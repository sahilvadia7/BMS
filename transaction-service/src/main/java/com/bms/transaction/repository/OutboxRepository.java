package com.bms.transaction.repository;

import com.bms.transaction.enums.OutboxStatus;
import com.bms.transaction.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {
	List<OutboxEvent> findTop10ByStatusOrderByCreatedAt(OutboxStatus outboxStatus);
}

