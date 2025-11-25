package com.bms.gateway.model;

import com.bms.gateway.enums.WebhookEventType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "webhook_events", indexes = {
		@Index(name = "idx_webhook_event_type", columnList = "eventType")
})
public class WebhookEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(nullable = false)
	private WebhookEventType eventType;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String payload;

	private String signature;

	@Column(nullable = false)
	private boolean processed = false;

	private int retryCount = 0;

	@CreationTimestamp
	private LocalDateTime receivedAt;
}
