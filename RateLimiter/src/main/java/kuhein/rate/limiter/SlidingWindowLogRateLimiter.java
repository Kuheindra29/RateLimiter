package kuhein.rate.limiter;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SlidingWindowLogRateLimiter implements RateLimiter {

    private static final int MAX_LIMIT = 100;
    private static final long WINDOW_MS = 60_000;

    private final Map<String, Deque<Long>> userRequests =
            new ConcurrentHashMap<>();

    @Override
    public boolean allowRequest(String userId) {

        long now = System.currentTimeMillis();

        Deque<Long> requests =
                userRequests.computeIfAbsent(
                        userId,
                        k -> new ArrayDeque<>());

        synchronized (requests) {

            while (!requests.isEmpty()
                    && now - requests.peekFirst() >= WINDOW_MS) {

                requests.pollFirst();
            }

            if (requests.size() >= MAX_LIMIT) {
                return false;
            }

            requests.addLast(now);
            return true;
        }
    }

    @Override
    public long getRetryAfterMs(String userId) {

        Deque<Long> requests = userRequests.get(userId);

        if (requests == null) {
            return 0;
        }

        long now = System.currentTimeMillis();

        synchronized (requests) {

            while (!requests.isEmpty()
                    && now - requests.peekFirst() >= WINDOW_MS) {

                requests.pollFirst();
            }

            if (requests.size() < MAX_LIMIT) {
                return 0;
            }

            return WINDOW_MS - (now - requests.peekFirst());
        }
    }
}
