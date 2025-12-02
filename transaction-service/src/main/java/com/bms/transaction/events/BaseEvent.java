package com.bms.transaction.events;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public abstract class BaseEvent {
	private String eventId = UUID.randomUUID().toString();
	private LocalDateTime eventTime = LocalDateTime.now();
}