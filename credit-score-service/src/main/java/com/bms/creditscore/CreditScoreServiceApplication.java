package com.bms.creditscore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CreditScoreServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CreditScoreServiceApplication.class, args);
	}

}
