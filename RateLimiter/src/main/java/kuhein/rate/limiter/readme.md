# API Gateway Rate Limiter

Implement a thread-safe rate limiter that limits each
user to 100 requests per minute using the Sliding Window
Log algorithm.

### Interface

RateLimiter

Methods:

- boolean allowRequest(String userId)
- long getRetryAfterMs(String userId)

### Implementation

SlidingWindowRateLimiter

Each user has:

- Independent request history
- Limit of 100 requests
- Sliding 60 second window

The Sliding Window Log algorithm stores timestamps
for every request.

For each incoming request:

1. Remove timestamps older than 60 seconds
2. Count remaining requests
3. Allow if below 100
4. Reject otherwise

The implementation uses:

- ConcurrentHashMap for user storage
- Synchronized access to each user's request queue

This ensures correctness under concurrent access while
allowing different users to be processed independently.

### Example
User A

Request 1-100  -> Allowed
Request 101    -> Rejected

Retry After -> milliseconds until oldest request
expires from the sliding window

### Limitations
The Sliding Window Log algorithm stores every request
timestamp, which may consume more memory for very
large numbers of users.


## Algorithm Comparison

**Sliding Window Log**
- Accurate and easy to understand
- Uses more memory because request timestamps are stored

**Token Bucket**
- More memory efficient
- Better supports burst traffic

**My Choice**

I implemented **Sliding Window Log** because it was the required algorithm and provides accurate rate limiting. I also added **Token Bucket** as an optional implementation to demonstrate extensibility through the same `RateLimiter` interface.
## Scaling Across Multiple Servers
For a distributed deployment, request data should be
stored in a shared datastore such as Redis.
This ensures all API gateway instances enforce the
same rate limit for a user.


