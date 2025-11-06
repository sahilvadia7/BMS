package com.bms.creditscore.service.impl;

import com.bms.creditscore.dto.extrenal.AccountSummaryDTO;
import com.bms.creditscore.dto.extrenal.CustomerFeignDTO;
import com.bms.creditscore.dto.extrenal.TransactionSummaryDTO;
import com.bms.creditscore.dto.request.CreditCheckRequest;
import com.bms.creditscore.dto.request.CreditScoreRequest;
import com.bms.creditscore.dto.response.CreditCheckResponse;
import com.bms.creditscore.dto.response.CreditScoreResponse;
import com.bms.creditscore.feign.CustomerClient;
import com.bms.creditscore.model.CreditProfile;
import com.bms.creditscore.repository.CreditProfileRepository;
import com.bms.creditscore.service.CreditScoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import com.bms.creditscore.feign.AccountClient;
import com.bms.creditscore.feign.TransactionClient;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class CreditScoreServiceImpl implements CreditScoreService {

    private final CustomerClient customerClient;
    private final AccountClient accountClient;
    private final TransactionClient transactionClient;
    private final CreditProfileRepository repository;

    @Value("${repo.rate:6.5}")
    private double repoRate;

    // Weights (must sum to 1.0)
    private final double wPayment = 0.35;
    private final double wUtil = 0.30;
    private final double wAge = 0.15;
    private final double wMix = 0.10;
    private final double wInq = 0.10;

    public CreditScoreServiceImpl(CustomerClient customerClient,
                                  AccountClient accountClient,
                                  TransactionClient transactionClient,
                                  CreditProfileRepository repository) {
        this.customerClient = customerClient;
        this.accountClient = accountClient;
        this.transactionClient = transactionClient;
        this.repository = repository;
    }

    @Override
    public double getRepoRate() {
        return repoRate;
    }

    @Override
    public CreditScoreResponse calculate(Long customerId, CreditScoreRequest request) {
        log.info("Calculating credit score for customerId: {}", customerId);

        String source = "MANUAL";
        Map<String, Object> breakdown = new HashMap<>();

        // Fetch credit factors
        CreditFactors factors = fetchCreditFactors(customerId, request);
        source = factors.source;

        // Normalize all factors to 0-100 scale
        double paymentScore = normalizePaymentHistory(factors.paymentHistory);
        double utilizationScore = normalizeUtilization(factors.creditUtilization);
        double mixScore = normalizeCreditMix(factors.creditMix);
        double ageScore = normalizeCreditAge(factors.creditAge);
        double inquiryScore = normalizeInquiries(factors.recentInquiries);

        // Calculate weighted base score (0-100)
        double baseScore = (paymentScore * wPayment)
                + (utilizationScore * wUtil)
                + (mixScore * wMix)
                + (ageScore * wAge)
                + (inquiryScore * wInq);

        // Apply repo rate adjustment
        double repoAdjustment = calculateRepoAdjustment(repoRate);

        // Scale to CIBIL range (300-900)
        double finalScoreDouble = scaleToRange(baseScore) - repoAdjustment;

        // Apply inquiry penalty directly to final score
        finalScoreDouble -= (factors.recentInquiries * 5); // -5 points per inquiry

        // Clamp to valid range
        int finalScore = (int) Math.round(Math.max(300, Math.min(900, finalScoreDouble)));
        String status = classify(finalScore);

        // Build breakdown for transparency
        breakdown.put("paymentHistory", factors.paymentHistory);
        breakdown.put("paymentScore", paymentScore);
        breakdown.put("creditUtilization", factors.creditUtilization);
        breakdown.put("utilizationScore", utilizationScore);
        breakdown.put("creditMix", factors.creditMix);
        breakdown.put("mixScore", mixScore);
        breakdown.put("creditAge", factors.creditAge);
        breakdown.put("ageScore", ageScore);
        breakdown.put("recentInquiries", factors.recentInquiries);
        breakdown.put("inquiryScore", inquiryScore);
        breakdown.put("inquiryPenalty", factors.recentInquiries * 5);
        breakdown.put("baseScore", baseScore);
        breakdown.put("repoRate", repoRate);
        breakdown.put("repoAdjustment", repoAdjustment);
        breakdown.put("finalScoreBeforeClamp", finalScoreDouble);

        // Persist the profile
        CreditProfile profile = buildCreditProfile(customerId, factors, finalScore, status, source);
        repository.save(profile);

        log.info("Credit score calculated: {} ({}) for customer: {}", finalScore, status, customerId);

        return new CreditScoreResponse(customerId, finalScore, repoRate, status, source, Instant.now(), breakdown);
    }

    @Override
    public CreditScoreResponse getLatestByCustomer(Long customerId) {
        log.info("Fetching latest credit score for customerId: {}", customerId);

        Optional<CreditProfile> opt = repository.findFirstByCustomerIdOrderByCalculatedAtDesc(customerId);
        if (opt.isEmpty()) {
            log.warn("No credit profile found for customerId: {}", customerId);
            return null;
        }

        CreditProfile p = opt.get();
        Map<String, Object> breakdown = buildBreakdownFromProfile(p);

        return new CreditScoreResponse(
                customerId,
                p.getCalculatedScore(),
                p.getRepoRate(),
                p.getScoreStatus(),
                p.getSource(),
                p.getCalculatedAt(),
                breakdown
        );
    }

    /**
     * Fetch credit factors from various sources
     */
    private CreditFactors fetchCreditFactors(Long customerId, CreditScoreRequest request) {
        CreditFactors factors = new CreditFactors();

        if (customerId == null) {
            // Manual calculation - use request values or defaults
            factors.source = "MANUAL";
            factors.paymentHistory = getOrDefault(request.getPaymentHistory(), 50.0);
            factors.creditUtilization = getOrDefault(request.getCreditUtilization(), 50.0);
            factors.creditMix = getOrDefault(request.getCreditMix(), 40.0);
            factors.creditAge = getOrDefault(request.getCreditAge(), 1.0);
            factors.recentInquiries = getOrDefault(request.getRecentInquiries(), 0);
            return factors;
        }

        try {
            // Fetch customer to verify existence
            CustomerFeignDTO customer = customerClient.getById(customerId).getBody();

            if (customer == null) {
                log.warn("Customer not found: {}", customerId);
                factors.source = "NEW";
                applyNewCustomerDefaults(factors, request);
                return factors;
            }

            log.info("Customer found: {}", customer.getEmail());
            factors.source = "EXISTING";

            // Fetch account and transaction data
            AccountSummaryDTO acc = fetchAccountSummary(customerId);
            TransactionSummaryDTO tx = fetchTransactionSummary(customerId);

            // Derive payment history
            if (tx != null && tx.getTotalPayments() > 0) {
                factors.paymentHistory = (tx.getOnTimePayments() / (double) tx.getTotalPayments()) * 100.0;
            } else {
                factors.paymentHistory = getOrDefault(request.getPaymentHistory(), 50.0);
            }

            // Derive credit utilization
            if (acc != null && acc.getTotalCreditLimit() > 0) {
                factors.creditUtilization = (acc.getTotalUsedCredit() / acc.getTotalCreditLimit()) * 100.0;
            } else {
                factors.creditUtilization = getOrDefault(request.getCreditUtilization(), 30.0);
            }

            // Derive credit mix (estimate based on account data)
            if (acc != null && acc.getTotalCreditLimit() > 0) {
                factors.creditMix = getOrDefault(request.getCreditMix(), 60.0);
            } else {
                factors.creditMix = getOrDefault(request.getCreditMix(), 40.0);
            }

            // Derive credit age
            if (tx != null && tx.getAccountAgeYears() > 0) {
                factors.creditAge = tx.getAccountAgeYears();
            } else {
                factors.creditAge = getOrDefault(request.getCreditAge(), 1.0);
            }

            // Derive recent inquiries
            if (tx != null) {
                factors.recentInquiries = tx.getRecentInquiries();
            } else {
                factors.recentInquiries = getOrDefault(request.getRecentInquiries(), 0);
            }

        } catch (Exception ex) {
            log.error("Error fetching customer data: {}", ex.getMessage());
            factors.source = "FALLBACK";
            applyFallbackDefaults(factors, request);
        }

        return factors;
    }

    private AccountSummaryDTO fetchAccountSummary(Long customerId) {
        try {
            return accountClient.getAccountSummary(customerId);
        } catch (Exception e) {
            log.warn("Failed to fetch account summary for customer {}: {}", customerId, e.getMessage());
            return null;
        }
    }

    private TransactionSummaryDTO fetchTransactionSummary(Long customerId) {
        try {
            return transactionClient.getTransactionSummary(customerId);
        } catch (Exception e) {
            log.warn("Failed to fetch transaction summary for customer {}: {}", customerId, e.getMessage());
            return null;
        }
    }

    /**
     * Normalize payment history to 0-100 score
     * Higher payment % = higher score
     */
    private double normalizePaymentHistory(double paymentHistoryPercent) {
        double clamped = Math.max(0, Math.min(100, paymentHistoryPercent));

        // Payment history is critical - apply exponential curve
        // 100% on-time = 100 score
        // 95% on-time = 90 score
        // 90% on-time = 75 score
        // 80% on-time = 50 score
        return Math.pow(clamped / 100.0, 0.5) * 100.0;
    }

    /**
     * Normalize credit utilization to 0-100 score
     * Lower utilization = higher score (inverted)
     */
    private double normalizeUtilization(double utilizationPercent) {
        double clamped = Math.max(0, Math.min(100, utilizationPercent));

        // Optimal utilization: 0-30%
        // Acceptable: 30-50%
        // High: 50-70%
        // Very high: 70-100%
        if (clamped <= 30) {
            return 100; // Perfect score
        } else if (clamped <= 50) {
            return 100 - ((clamped - 30) * 1.0); // Gradual decrease
        } else if (clamped <= 70) {
            return 80 - ((clamped - 50) * 1.5); // Faster decrease
        } else {
            return 50 - ((clamped - 70) * 1.0); // Penalty zone
        }
    }

    /**
     * Normalize credit mix to 0-100 score
     */
    private double normalizeCreditMix(double creditMixPercent) {
        return Math.max(0, Math.min(100, creditMixPercent));
    }

    /**
     * Normalize credit age to 0-100 score
     * Longer history = better score
     */
    private double normalizeCreditAge(double ageYears) {
        double clamped = Math.max(0, Math.min(30, ageYears)); // Cap at 30 years

        // 0 years = 0 score
        // 1 year = 30 score
        // 3 years = 60 score
        // 5 years = 80 score
        // 10+ years = 100 score
        if (clamped >= 10) {
            return 100;
        } else if (clamped >= 5) {
            return 80 + ((clamped - 5) / 5.0) * 20;
        } else if (clamped >= 3) {
            return 60 + ((clamped - 3) / 2.0) * 20;
        } else if (clamped >= 1) {
            return 30 + ((clamped - 1) / 2.0) * 30;
        } else {
            return clamped * 30; // 0-1 year linear
        }
    }

    /**
     * Normalize inquiries to 0-100 score
     * Fewer inquiries = better score
     */
    private double normalizeInquiries(int inquiryCount) {
        int clamped = Math.max(0, Math.min(10, inquiryCount)); // Cap at 10

        // 0 inquiries = 100 score
        // 1-2 inquiries = 90 score
        // 3-4 inquiries = 70 score
        // 5+ inquiries = 50 score or less
        return Math.max(0, 100 - (clamped * 10.0));
    }

    /**
     * Calculate repo rate adjustment
     */
    private double calculateRepoAdjustment(double currentRepoRate) {
        // Baseline repo rate is 6.5%
        // For every 0.5% increase in repo rate, reduce score by ~5 points
        double baselineRate = 6.5;
        double rateDiff = currentRepoRate - baselineRate;
        return rateDiff * 10.0; // 10 points per 1% change
    }

    /**
     * Scale base score (0-100) to CIBIL range (300-900)
     */
    private double scaleToRange(double baseScore) {
        // Linear scaling: 0 -> 300, 100 -> 900
        return 300 + (baseScore / 100.0) * 600;
    }

    /**
     * Classify score into categories
     */
    private String classify(int score) {
        if (score >= 750) return "EXCELLENT";
        if (score >= 700) return "GOOD";
        if (score >= 650) return "FAIR";
        if (score >= 600) return "AVERAGE";
        return "POOR";
    }

    private void applyNewCustomerDefaults(CreditFactors factors, CreditScoreRequest request) {
        factors.paymentHistory = getOrDefault(request.getPaymentHistory(), 50.0);
        factors.creditUtilization = getOrDefault(request.getCreditUtilization(), 50.0);
        factors.creditMix = getOrDefault(request.getCreditMix(), 40.0);
        factors.creditAge = getOrDefault(request.getCreditAge(), 0.5);
        factors.recentInquiries = getOrDefault(request.getRecentInquiries(), 1);
    }

    private void applyFallbackDefaults(CreditFactors factors, CreditScoreRequest request) {
        factors.paymentHistory = getOrDefault(request.getPaymentHistory(), 50.0);
        factors.creditUtilization = getOrDefault(request.getCreditUtilization(), 50.0);
        factors.creditMix = getOrDefault(request.getCreditMix(), 40.0);
        factors.creditAge = getOrDefault(request.getCreditAge(), 1.0);
        factors.recentInquiries = getOrDefault(request.getRecentInquiries(), 0);
    }

    private double getOrDefault(Double value, double defaultValue) {
        return Optional.ofNullable(value).orElse(defaultValue);
    }

    private int getOrDefault(Integer value, int defaultValue) {
        return Optional.ofNullable(value).orElse(defaultValue);
    }

    private CreditProfile buildCreditProfile(Long customerId, CreditFactors factors,
                                             int finalScore, String status, String source) {
        return CreditProfile.builder()
                .customerId(customerId)
                .paymentHistory(factors.paymentHistory)
                .creditUtilization(factors.creditUtilization)
                .creditMix(factors.creditMix)
                .creditAge(factors.creditAge)
                .recentInquiries(factors.recentInquiries)
                .calculatedScore(finalScore)
                .scoreStatus(status)
                .repoRate(repoRate)
                .source(source)
                .calculatedAt(Instant.now())
                .build();
    }

    private Map<String, Object> buildBreakdownFromProfile(CreditProfile p) {
        Map<String, Object> breakdown = new HashMap<>();
        breakdown.put("paymentHistory", p.getPaymentHistory());
        breakdown.put("creditUtilization", p.getCreditUtilization());
        breakdown.put("creditMix", p.getCreditMix());
        breakdown.put("creditAge", p.getCreditAge());
        breakdown.put("recentInquiries", p.getRecentInquiries());
        breakdown.put("repoRate", p.getRepoRate());
        return breakdown;
    }

    @Override
    public CreditCheckResponse checkEligibility(CreditCheckRequest request) {
        log.info("Checking credit eligibility for customerId: {}", request.getCustomerId());

        Long customerId = request.getCustomerId();
        if (customerId == null) {
            return CreditCheckResponse.builder()
                    .eligible(false)
                    .message("Customer ID is required")
                    .cibilScore(0)
                    .minimumScoreRequired(650.0)
                    .status("NOT_AVAILABLE")
                    .build();
        }

        // Try to get latest score first
        CreditScoreResponse latestScore = getLatestByCustomer(customerId);

        // If no score exists, calculate one
        if (latestScore == null) {
            log.info("No existing score found, calculating new score for customer: {}", customerId);
            CreditScoreRequest calcRequest = new CreditScoreRequest();
            calcRequest.setCustomerId(customerId);
            latestScore = calculate(customerId, calcRequest);
        }

        // Check eligibility based on score thresholds
        int score = latestScore.getCibilScore();
        double minScoreRequired = 650.0; // Minimum for loan eligibility

        boolean eligible = score >= minScoreRequired;
        String message = eligible
                ? "Customer is eligible for credit products"
                : String.format("Credit score %d is below minimum required %d", score, (int) minScoreRequired);

        return CreditCheckResponse.builder()
                .customerId(customerId)
                .eligible(eligible)
                .cibilScore(score)
                .status(latestScore.getStatus())
                .message(message)
                .minimumScoreRequired(minScoreRequired)
                .build();
    }

    @Override
    public List<CreditScoreResponse> getCreditHistory(Long customerId) {
        log.info("Fetching credit history for customerId: {}", customerId);

        List<CreditProfile> profiles = repository.findByCustomerIdOrderByCalculatedAtDesc(customerId);

        return profiles.stream()
                .map(p -> new CreditScoreResponse(
                        p.getCustomerId(),
                        p.getCalculatedScore(),
                        p.getRepoRate(),
                        p.getScoreStatus(),
                        p.getSource(),
                        p.getCalculatedAt(),
                        buildBreakdownFromProfile(p)
                ))
                .toList();
    }

    @Override
    public List<CreditScoreResponse> getAllProfiles() {
        log.info("Fetching all credit profiles");

        List<CreditProfile> profiles = repository.findAll();

        return profiles.stream()
                .map(p -> new CreditScoreResponse(
                        p.getCustomerId(),
                        p.getCalculatedScore(),
                        p.getRepoRate(),
                        p.getScoreStatus(),
                        p.getSource(),
                        p.getCalculatedAt(),
                        buildBreakdownFromProfile(p)
                ))
                .toList();
    }

    /**
     * Internal class to hold credit factors
     */
    private static class CreditFactors {
        String source;
        double paymentHistory;
        double creditUtilization;
        double creditMix;
        double creditAge;
        int recentInquiries;
    }
}