package com.bms.transaction.repository;

import com.bms.transaction.enums.OutboxStatus;
import com.bms.transaction.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxEvent,String> {
	List<OutboxEvent> findTop10ByStatusOrderByCreatedAt(OutboxStatus outboxStatus);
}

