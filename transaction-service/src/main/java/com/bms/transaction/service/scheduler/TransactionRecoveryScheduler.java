package com.bms.transaction.service.scheduler;

import com.bms.transaction.repository.TransactionRepository;
import com.bms.transaction.service.InternalTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@EnableScheduling
public class TransactionRecoveryScheduler {

	@Autowired
	private TransactionRepository transactionRepository;

	@Autowired
	private InternalTransactionService transactionService;

}
