package br.com.gustavo.coupon;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;

import br.com.gustavo.coupon.adapters.out.persistence.CouponJpaRepository;

@SpringBootTest
@AutoConfigureMockMvc
class CouponApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CouponJpaRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAllInBatch();
    }

    @Test
    void shouldCreateCouponAndReturn201() throws Exception {
        String futureDate = OffsetDateTime.now(ZoneOffset.UTC).plusDays(10).toString();
        
        String jsonPayload = """
                {
                  "code": "INT-123!",
                  "description": "Cupom de Integração",
                  "discountValue": 25.0,
                  "expirationDate": "%s",
                  "published": true
                }
                """.formatted(futureDate);

        mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.code").value("INT123"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.redeemed").value(false))
                .andExpect(jsonPath("$.description").value("Cupom de Integração"));
    }

    @Test
    void shouldReturn400WhenValidationFails() throws Exception {
        String pastDate = OffsetDateTime.now(ZoneOffset.UTC).minusDays(5).toString();
        
        String jsonPayload = """
                {
                  "code": "ERR",
                  "description": "",
                  "discountValue": -5.0,
                  "expirationDate": "%s",
                  "published": true
                }
                """.formatted(pastDate);

        mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.fieldErrors.description").exists())
                .andExpect(jsonPath("$.fieldErrors.discountValue").exists())
                .andExpect(jsonPath("$.fieldErrors.expirationDate").exists());
    }

    @Test
    void shouldReturn422WhenBusinessRuleFails() throws Exception {
        String futureDate = OffsetDateTime.now(ZoneOffset.UTC).plusDays(10).toString();
        
        String jsonPayload = """
                {
                  "code": "VALID1",
                  "description": "Desconto Baixo",
                  "discountValue": 0.1,
                  "expirationDate": "%s",
                  "published": true
                }
                """.formatted(futureDate);

        mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.message").value("Discount value must be at least 0.5"));
    }

    @Test
    void shouldDeleteCouponAndReturn204() throws Exception {
        String futureDate = OffsetDateTime.now(ZoneOffset.UTC).plusDays(10).toString();
        
        String jsonPayload = """
                {
                  "code": "DEL123",
                  "description": "Cupom para deletar",
                  "discountValue": 10.0,
                  "expirationDate": "%s",
                  "published": true
                }
                """.formatted(futureDate);

        MvcResult createResult = mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andReturn();

        String id = JsonPath.read(createResult.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(delete("/coupon/" + id))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn422WhenCreatingCouponWithExistingActiveCode() throws Exception {
        String futureDate = OffsetDateTime.now(ZoneOffset.UTC).plusDays(10).toString();

        String firstPayload = """
                {
                  "code": "DUP111",
                  "description": "Cupom Original",
                  "discountValue": 10.0,
                  "expirationDate": "%s",
                  "published": true
                }
                """.formatted(futureDate);

        String secondPayload = """
                {
                  "code": "DUP111",
                  "description": "Cupom Alterado",
                  "discountValue": 20.0,
                  "expirationDate": "%s",
                  "published": false
                }
                """.formatted(futureDate);

        mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstPayload))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondPayload))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.message").value("Coupon already exists with code: DUP111"));

        assertEquals(
                "Cupom Original",
                repository.findByCode("DUP111").orElseThrow().getDescription()
        );
    }

    @Test
    void shouldReturn422WhenCreatingCouponWithCodeFromDeletedCoupon() throws Exception {
        String futureDate = OffsetDateTime.now(ZoneOffset.UTC).plusDays(10).toString();

        String payload = """
                {
                  "code": "DEL321",
                  "description": "Cupom Repetido",
                  "discountValue": 10.0,
                  "expirationDate": "%s",
                  "published": true
                }
                """.formatted(futureDate);

        MvcResult createResult = mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        String id = JsonPath.read(createResult.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(delete("/coupon/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.message").value("Coupon already exists with code: DEL321"));
    }
}
