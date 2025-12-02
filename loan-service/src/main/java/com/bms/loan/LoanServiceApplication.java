package com.bms.loan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableFeignClients
@EnableScheduling
public class LoanServiceApplication {

	public static void main(String[] args) {
        System.out.println("jna.library.path=" + System.getProperty("jna.library.path"));
        System.out.println("java.library.path=" + System.getProperty("java.library.path"));
        SpringApplication.run(LoanServiceApplication.class, args);
	}

}
