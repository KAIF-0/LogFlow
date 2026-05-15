package com.example.log_flow.config.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final long requestsPerMinute;
    private final long burstCapacity;

    public RateLimitService(@Value("${rate-limit.requests-per-minute}") long requestsPerMinute,
                            @Value("${rate-limit.burst-capacity}") long burstCapacity) {
        this.requestsPerMinute = requestsPerMinute;
        this.burstCapacity = burstCapacity;
    }

    public boolean tryConsume(String key) {
        return buckets.computeIfAbsent(key, this::createBucket).tryConsume(1);
    }

    private Bucket createBucket(String key) {
        Bandwidth limit = Bandwidth.classic(burstCapacity, Refill.greedy(requestsPerMinute, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
}
