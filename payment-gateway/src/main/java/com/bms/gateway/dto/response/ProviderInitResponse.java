package com.bms.gateway.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProviderInitResponse {

	private String providerReference;
	private String rawResponse;
}
