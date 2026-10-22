package kuhein.rate.limiter;

public class Main {

    public static void main(String[] args) {

        RateLimiter rateLimiter =
                new SlidingWindowLogRateLimiter();

        String userId = "user-123";

        for (int i = 1; i <= 105; i++) {

            boolean allowed =
                    rateLimiter.allowRequest(userId);

            System.out.println(
                    "Request " + i + ": " + allowed
            );
        }

        System.out.println(
                "Retry After(ms): " +
                        rateLimiter.getRetryAfterMs(userId)
        );
    }
}