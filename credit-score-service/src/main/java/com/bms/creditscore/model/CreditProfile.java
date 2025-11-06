package com.bms.creditscore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
import java.time.Instant;

@Entity
@Table(name = "credit_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditProfile {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "customer_id")
    private Long customerId;

    private double paymentHistory;
    private double creditUtilization;
    private double creditMix;
    private double creditAge;
    private int recentInquiries;

    @Column(name = "calculated_score")
    private int calculatedScore;

    @Column(name = "score_status", length = 20)
    private String scoreStatus;

    @Column(name = "repo_rate")
    private double repoRate;

    @Column(name = "source", length = 20)
    private String source;

    @Column(name = "calculated_at")
    private Instant calculatedAt;
}
