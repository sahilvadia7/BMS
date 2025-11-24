package com.bms.transaction.model;
import com.bms.transaction.enums.Channel;
import com.bms.transaction.enums.Currency;
import com.bms.transaction.enums.TransactionStatus;
import com.bms.transaction.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "destination_account_number")
    private String destinationAccountNumber;

    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "amount", nullable = false, precision = 11, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false)
    private Currency currency;

    @Column(name = "fee", precision = 11, scale = 2)
    private BigDecimal fee;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    @Column(name = "description")
    private String description;

    @Column(name = "reference_id", unique = true)
    private String referenceId;

    @Column(name = "channel")
    @Enumerated(EnumType.STRING)
    private Channel channel;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "next_retry_time")
    private LocalDateTime nextRetryTime;

    @Column(name = "retry_count")
    private int retryCount = 0;

    @Column(name = "is_chargeable")
    private boolean isChargeable=false;

    @Column(name = "linked_transaction_id")
    private String linkedTransactionId;


    @PrePersist
    public void prePersist() {
        if (this.transactionDate == null)
            this.transactionDate = LocalDateTime.now();

        if (this.transactionId == null)
            this.transactionId = "TXN" + System.currentTimeMillis();
    }
}
