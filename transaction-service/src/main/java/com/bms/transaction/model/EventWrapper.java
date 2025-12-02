package com.bms.transaction.model;

import lombok.Data;

@Data
public class EventWrapper {
	private String type;
	private Object payload;
}