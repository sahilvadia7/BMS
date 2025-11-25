package com.bms.transaction.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatementResult {
	private byte[] pdfBytes;
	private String customerName;
	private String email;
}

