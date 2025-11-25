package com.bms.transaction.feing;

import com.bms.transaction.config.FeignMultipartConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(
		path = "/api/v1/notifications",
		name = "notification-service",
		configuration = FeignMultipartConfig.class
)
public interface NotificationClient {

	@PostMapping(
			value = "/send-transaction-statement",
			consumes = MediaType.MULTIPART_FORM_DATA_VALUE
	)
	ResponseEntity<String> sendStatement(
			@RequestPart("accountNumber") String accountNumber,
			@RequestPart("name") String name,
			@RequestPart("toEmail") String toEmail,
			@RequestPart("file") MultipartFile file
	);
}



