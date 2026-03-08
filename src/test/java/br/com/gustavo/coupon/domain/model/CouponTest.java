package br.com.gustavo.coupon.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import br.com.gustavo.shared.exception.BusinessException;

class CouponTest {

    @Test
    void shouldCreateCouponSuccessfullyAndSanitizeCode() {
        String rawCode = "A B@C-1#2$3!";
        
        Coupon coupon = Coupon.create(
                rawCode,
                "Cupom de Teste",
                new BigDecimal("10.0"),
                OffsetDateTime.now().plusDays(10),
                true
        );

        assertNotNull(coupon);
        assertEquals("ABC123", coupon.getCode());
        assertTrue(coupon.isPublished());
        assertFalse(coupon.isDeleted());
    }

    @Test
    void shouldThrowExceptionWhenSanitizedCodeDoesNotHave6Characters() {
        String invalidCode = "AB12";

        BusinessException exception = assertThrows(BusinessException.class, () -> 
            Coupon.create(invalidCode, "Desc", new BigDecimal("1.0"), OffsetDateTime.now().plusDays(1), true)
        );

        assertEquals("Code must have exactly 6 alphanumeric characters after sanitization", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenExpirationDateIsInThePast() {
        OffsetDateTime pastDate = OffsetDateTime.now().minusDays(1);

        BusinessException exception = assertThrows(BusinessException.class, () -> 
            Coupon.create("ABC123", "Desc", new BigDecimal("1.0"), pastDate, true)
        );

        assertEquals("Expiration date cannot be in the past", exception.getMessage());
    }

    @Test
    void shouldRehydrateCouponSuccessfullyEvenWhenExpired() {
        UUID id = UUID.randomUUID();
        OffsetDateTime expiredDate = OffsetDateTime.now().minusDays(1);

        Coupon coupon = Coupon.rehydrate(
                id,
                "ABC123",
                "Desc",
                new BigDecimal("1.0"),
                expiredDate,
                "ACTIVE",
                true,
                false,
                false,
                null
        );

        assertNotNull(coupon);
        assertEquals(id, coupon.getId());
        assertEquals(expiredDate, coupon.getExpirationDate());
    }

    @Test
    void shouldThrowExceptionWhenDiscountIsLessThanMinimum() {
        BigDecimal invalidDiscount = new BigDecimal("0.49");

        BusinessException exception = assertThrows(BusinessException.class, () -> 
            Coupon.create("ABC123", "Desc", invalidDiscount, OffsetDateTime.now().plusDays(1), true)
        );

        assertEquals("Discount value must be at least 0.5", exception.getMessage());
    }

    @Test
    void shouldSoftDeleteCouponSuccessfully() {
        Coupon coupon = Coupon.create("ABC123", "Desc", new BigDecimal("1.0"), OffsetDateTime.now().plusDays(1), true);
        
        coupon.delete();

        assertTrue(coupon.isDeleted());
        assertNotNull(coupon.getDeletedAt());
    }

    @Test
    void shouldThrowExceptionWhenDeletingAlreadyDeletedCoupon() {
        Coupon coupon = Coupon.create("ABC123", "Desc", new BigDecimal("1.0"), OffsetDateTime.now().plusDays(1), true);
        coupon.delete();

        BusinessException exception = assertThrows(BusinessException.class, coupon::delete);

        assertEquals("Coupon already deleted", exception.getMessage());
    }

    @Test
    void shouldRehydrateDeletedCouponKeepingOriginalDeletedAt() {
        UUID id = UUID.randomUUID();
        OffsetDateTime originalDeletedAt = OffsetDateTime.now().minusHours(2);

        Coupon coupon = Coupon.rehydrate(
                id,
                "ABC123",
                "Desc",
                new BigDecimal("1.0"),
                OffsetDateTime.now().plusDays(1),
                "ACTIVE",
                true,
                false,
                true,
                originalDeletedAt
        );

        assertTrue(coupon.isDeleted());
        assertEquals(originalDeletedAt, coupon.getDeletedAt());
    }
}
