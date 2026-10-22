package kuhein.rate.limiter;

import java.util.concurrent.ConcurrentHashMap;

public class TokenBucketRateLimiter implements RateLimiter {

    private static final int CAPACITY = 100;
    private static final double REFILL_PER_MS =
            100.0 / 60_000.0;

    private final ConcurrentHashMap<String, Bucket> buckets =
            new ConcurrentHashMap<>();

    @Override
    public boolean allowRequest(String userId) {

        Bucket bucket =
                buckets.computeIfAbsent(userId,
                        k -> new Bucket());

        synchronized (bucket) {

            refill(bucket);

            if (bucket.tokens < 1) {
                return false;
            }

            bucket.tokens -= 1;

            return true;
        }
    }

    @Override
    public long getRetryAfterMs(String userId) {

        Bucket bucket = buckets.get(userId);

        if (bucket == null) {
            return 0;
        }

        synchronized (bucket) {

            refill(bucket);

            if (bucket.tokens >= 1) {
                return 0;
            }

            return (long) Math.ceil(
                    (1 - bucket.tokens) / REFILL_PER_MS
            );
        }
    }

    private void refill(Bucket bucket) {

        long now = System.currentTimeMillis();

        long elapsed = now - bucket.lastRefillTime;

        double newTokens =
                elapsed * REFILL_PER_MS;

        bucket.tokens =
                Math.min(CAPACITY,
                        bucket.tokens + newTokens);

        bucket.lastRefillTime = now;
    }

    private static class Bucket {

        double tokens = CAPACITY;

        long lastRefillTime =
                System.currentTimeMillis();
    }
}
