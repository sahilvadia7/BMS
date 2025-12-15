//package com.bms.account.utility;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Service;
//
//import java.util.concurrent.TimeUnit;
//
//@Slf4j
//@Service
//public class PinAttemptService {
//
//    private static final String PREFIX = "PIN_ATTEMPTS::";
//
//    @Value("${security.pin.max-attempts:3}")
//    private int MAX_ATTEMPTS;
//
//    @Value("${security.pin.lock-duration-minutes:1}")
//    private long LOCK_MINUTES;
//
//    private final RedisTemplate<String, Object> redisTemplate;
//
//    public PinAttemptService(RedisTemplate<String, Object> redisTemplate) {
//        this.redisTemplate = redisTemplate;
//    }
//
//    private String buildKey(String accountNumber) {
//        return PREFIX + accountNumber;
//    }
//
//    /** Check account lock */
//    public boolean isAccountLocked(String accountNumber) {
//        int attempts = getAttempts(accountNumber);
//        long ttl = getRemainingLockTime(accountNumber) == null ? -1 : getRemainingLockTime(accountNumber);
//
//        boolean locked = attempts >= MAX_ATTEMPTS && ttl > 0;
//
//        if (locked) {
//            log.error(" Access Denied: Account {} is locked! TTL remaining = {} sec", accountNumber, ttl);
//        }
//
//        return locked;
//    }
//
//
//    /** Record a failed attempt */
//    public int recordFailedAttempt(String accountNumber) {
//        String key = buildKey(accountNumber);
//        Long attempts = redisTemplate.opsForValue().increment(key);
//
//        // Apply lock TTL on FIRST fail or when lock condition reached
//        if (attempts == 1 || attempts >= MAX_ATTEMPTS) {
//            redisTemplate.expire(key, LOCK_MINUTES, TimeUnit.MINUTES);
//        }
//
//        boolean locked = attempts >= MAX_ATTEMPTS;
//
//        if (locked) {
//            Long ttl = getRemainingLockTime(accountNumber);
//            log.error(" Account {} LOCKED! Failed attempts = {}, Remaining lock time = {} sec",
//                    accountNumber, attempts, ttl);
//        } else {
//            log.warn("⚠ Account {} failed attempts = {}",
//                    accountNumber, attempts);
//        }
//
//        return attempts.intValue();
//    }
//
//
//    /** Reset attempts after successful PIN match */
//    public void resetAttempts(String accountNumber) {
//        redisTemplate.delete(buildKey(accountNumber));
//        log.info("Reset PIN attempts for {}", accountNumber);
//    }
//
//    public int getAttempts(String accountNumber) {
//        Object value = redisTemplate.opsForValue().get(buildKey(accountNumber));
//        return value == null ? 0 : Integer.parseInt(value.toString());
//    }
//
//    /** Remaining lock time (seconds) */
//    public Long getRemainingLockTime(String accountNumber) {
//        return redisTemplate.getExpire(buildKey(accountNumber), TimeUnit.SECONDS);
//    }
//
//    public int getMaxAttempts() {
//        return MAX_ATTEMPTS;
//    }
//}
