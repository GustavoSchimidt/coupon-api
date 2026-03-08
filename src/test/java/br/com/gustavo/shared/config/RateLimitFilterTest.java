package br.com.gustavo.shared.config;

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitFilterTest {

    @Test
    void shouldBypassWhenRateLimitIsDisabled() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(false, 1, 60);

        ExecutionResult result = execute(filter, "/coupon", "10.0.0.1", null);

        assertEquals(1, result.chainCalls());
        assertEquals(200, result.response().getStatus());
    }

    @Test
    void shouldBypassWhenPathIsNotCoupon() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(true, 1, 60);

        ExecutionResult result = execute(filter, "/health", "10.0.0.1", null);

        assertEquals(1, result.chainCalls());
        assertEquals(200, result.response().getStatus());
    }

    @Test
    void shouldUseRemoteAddrWhenForwardedHeaderIsMissingOrBlank() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(true, 1, 60);

        ExecutionResult first = execute(filter, "/coupon", "10.0.0.1", "   ");
        ExecutionResult second = execute(filter, "/coupon", "10.0.0.1", null);

        assertEquals(1, first.chainCalls());
        assertEquals(0, second.chainCalls());
        assertEquals(429, second.response().getStatus());
    }

    @Test
    void shouldUseFirstForwardedForIpAsRateLimitKey() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(true, 1, 60);

        ExecutionResult first = execute(filter, "/coupon", "10.0.0.1", "9.9.9.9, 8.8.8.8");
        ExecutionResult second = execute(filter, "/coupon", "10.0.0.2", "9.9.9.9, 7.7.7.7");
        ExecutionResult third = execute(filter, "/coupon", "10.0.0.2", "6.6.6.6");

        assertEquals(1, first.chainCalls());
        assertEquals(0, second.chainCalls());
        assertEquals(429, second.response().getStatus());
        assertEquals(1, third.chainCalls());
    }

    @Test
    void shouldReturn429WithRetryAfterAndBodyWhenLimitIsExceeded() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(true, 1, 60);

        execute(filter, "/coupon", "10.0.0.1", null);
        ExecutionResult blocked = execute(filter, "/coupon", "10.0.0.1", null);

        assertEquals(0, blocked.chainCalls());
        assertEquals(429, blocked.response().getStatus());
        assertTrue(blocked.response().getHeader("Retry-After") != null);
        assertTrue(blocked.response().getContentAsString().contains("Too Many Requests"));
        assertTrue(blocked.response().getContentAsString().contains("\"path\":\"/coupon\""));
    }

    @Test
    void shouldResetWindowAfterConfiguredTime() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(true, 1, 1);

        ExecutionResult first = execute(filter, "/coupon", "10.0.0.1", null);
        ExecutionResult blocked = execute(filter, "/coupon", "10.0.0.1", null);

        Thread.sleep(1200);

        ExecutionResult afterWindow = execute(filter, "/coupon", "10.0.0.1", null);

        assertEquals(1, first.chainCalls());
        assertEquals(429, blocked.response().getStatus());
        assertEquals(1, afterWindow.chainCalls());
        assertEquals(200, afterWindow.response().getStatus());
    }

    @Test
    void shouldCleanUpOldCounters() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(true, 5, 1);

        execute(filter, "/coupon", "10.0.0.1", null);
        Thread.sleep(1200);
        execute(filter, "/coupon", "10.0.0.2", null);
        Thread.sleep(1200);
        execute(filter, "/coupon", "10.0.0.3", null);

        Map<?, ?> counters = counters(filter);
        assertTrue(counters.size() <= 2);
    }

    @Test
    void shouldBypassWhenRequestUriIsNull() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(true, 1, 60);

        HttpServletRequest request = new HttpServletRequestWrapper(new MockHttpServletRequest()) {
            @Override
            public String getRequestURI() {
                return null;
            }
        };
        MockHttpServletResponse response = new MockHttpServletResponse();
        RecordingFilterChain chain = new RecordingFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(1, chain.calls);
        assertEquals(200, response.getStatus());
    }

    private ExecutionResult execute(RateLimitFilter filter, String uri, String remoteAddr, String forwardedFor)
            throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setRemoteAddr(remoteAddr);
        if (forwardedFor != null) {
            request.addHeader("X-Forwarded-For", forwardedFor);
        }

        MockHttpServletResponse response = new MockHttpServletResponse();
        RecordingFilterChain chain = new RecordingFilterChain();
        filter.doFilter(request, response, chain);
        return new ExecutionResult(response, chain.calls);
    }

    @SuppressWarnings("unchecked")
    private Map<?, ?> counters(RateLimitFilter filter) throws NoSuchFieldException, IllegalAccessException {
        Field field = RateLimitFilter.class.getDeclaredField("counters");
        field.setAccessible(true);
        return (Map<?, ?>) field.get(filter);
    }

    private record ExecutionResult(MockHttpServletResponse response, int chainCalls) {}

    private static final class RecordingFilterChain implements FilterChain {
        private int calls = 0;

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) throws ServletException {
            calls++;
        }
    }
}
