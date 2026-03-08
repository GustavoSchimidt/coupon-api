package br.com.gustavo.coupon;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.gustavo.coupon.adapters.out.persistence.CouponJpaRepository;

@SpringBootTest(properties = {
        "rate.limit.enabled=true",
        "rate.limit.max-requests=2",
        "rate.limit.window-seconds=60"
})
@AutoConfigureMockMvc
class RateLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CouponJpaRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAllInBatch();
    }

    @Test
    void shouldReturn429WhenRateLimitIsExceeded() throws Exception {
        String futureDate = OffsetDateTime.now(ZoneOffset.UTC).plusDays(10).toString();

        String payload1 = """
                {
                  "code": "RAT001",
                  "description": "Cupom 1",
                  "discountValue": 10.0,
                  "expirationDate": "%s",
                  "published": true
                }
                """.formatted(futureDate);

        String payload2 = """
                {
                  "code": "RAT002",
                  "description": "Cupom 2",
                  "discountValue": 10.0,
                  "expirationDate": "%s",
                  "published": true
                }
                """.formatted(futureDate);

        String payload3 = """
                {
                  "code": "RAT003",
                  "description": "Cupom 3",
                  "discountValue": 10.0,
                  "expirationDate": "%s",
                  "published": true
                }
                """.formatted(futureDate);

        mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload1))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload2))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload3))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.error").value("Too Many Requests"))
                .andExpect(jsonPath("$.message").value("Rate limit exceeded. Try again later."))
                .andExpect(jsonPath("$.path").value("/coupon"));
    }
}
