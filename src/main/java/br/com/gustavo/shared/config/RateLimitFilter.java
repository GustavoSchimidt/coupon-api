package br.com.gustavo.shared.config;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final boolean enabled;
    private final int maxRequests;
    private final long windowSeconds;
    private final Map<String, CounterWindow> counters = new ConcurrentHashMap<>();
    private final AtomicLong lastCleanupAt = new AtomicLong(System.currentTimeMillis());

    public RateLimitFilter(
            @Value("${rate.limit.enabled:true}") boolean enabled,
            @Value("${rate.limit.max-requests:60}") int maxRequests,
            @Value("${rate.limit.window-seconds:60}") long windowSeconds
    ) {
        this.enabled = enabled;
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!enabled || !isCouponEndpoint(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        long now = System.currentTimeMillis();
        cleanupIfNeeded(now);

        Decision decision = consume(resolveClientKey(request), now);
        if (decision.allowed()) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Retry-After", String.valueOf(decision.retryAfterSeconds()));
        response.getWriter().write(buildJsonResponse(request.getRequestURI()));
    }

    private Decision consume(String key, long now) {
        long windowMillis = windowSeconds * 1000L;
        CounterWindow counter = counters.computeIfAbsent(key, ignored -> new CounterWindow(now));

        synchronized (counter) {
            if (now - counter.windowStartAt >= windowMillis) {
                counter.windowStartAt = now;
                counter.requests.set(0);
            }

            int current = counter.requests.incrementAndGet();
            if (current <= maxRequests) {
                return new Decision(true, 0);
            }

            long remainingMillis = Math.max(1, windowMillis - (now - counter.windowStartAt));
            long retryAfterSeconds = Math.max(1, (remainingMillis + 999) / 1000);
            return new Decision(false, retryAfterSeconds);
        }
    }

    private void cleanupIfNeeded(long now) {
        long windowMillis = windowSeconds * 1000L;
        long last = lastCleanupAt.get();
        if (now - last < windowMillis) {
            return;
        }

        if (lastCleanupAt.compareAndSet(last, now)) {
            counters.entrySet().removeIf(entry -> now - entry.getValue().windowStartAt >= (windowMillis * 2));
        }
    }

    private boolean isCouponEndpoint(String uri) {
        return uri != null && uri.startsWith("/coupon");
    }

    private String resolveClientKey(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String buildJsonResponse(String path) {
        String safePath = path == null ? "" : path.replace("\"", "");
        return """
                {"timestamp":"%s","status":429,"error":"Too Many Requests","message":"Rate limit exceeded. Try again later.","path":"%s"}
                """.formatted(LocalDateTime.now(), safePath).trim();
    }

    private record Decision(boolean allowed, long retryAfterSeconds) {}

    private static final class CounterWindow {
        private final AtomicInteger requests = new AtomicInteger(0);
        private long windowStartAt;

        private CounterWindow(long windowStartAt) {
            this.windowStartAt = windowStartAt;
        }
    }
}
