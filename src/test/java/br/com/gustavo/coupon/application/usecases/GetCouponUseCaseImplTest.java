package br.com.gustavo.coupon.application.usecases;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.gustavo.coupon.application.ports.out.CouponRepositoryPort;
import br.com.gustavo.coupon.domain.model.Coupon;
import br.com.gustavo.shared.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class GetCouponUseCaseImplTest {

    @Mock
    private CouponRepositoryPort repositoryPort;

    @InjectMocks
    private GetCouponUseCaseImpl useCase;

    @Test
    void shouldGetCouponSuccessfullyWhenFound() {
        UUID id = UUID.randomUUID();
        Coupon existingCoupon = Coupon.rehydrate(
                id,
                "TEST10",
                "Desc",
                new BigDecimal("10"),
                OffsetDateTime.now().plusDays(5),
                "ACTIVE",
                true,
                false,
                false,
                null
        );
        
        when(repositoryPort.findByCode(existingCoupon.getCode())).thenReturn(Optional.of(existingCoupon));

        var result = useCase.execute(existingCoupon.getCode());

        assertNotNull(result);
        assertEquals(existingCoupon.getId(), result.getId());
        assertEquals(existingCoupon.getCode(), result.getCode());
        assertEquals(existingCoupon.getDescription(), result.getDescription());
        assertEquals(existingCoupon.getDiscountValue(), result.getDiscountValue());
        assertEquals(existingCoupon.getExpirationDate(), result.getExpirationDate());
        assertEquals(existingCoupon.getStatus(), result.getStatus());
        assertEquals(existingCoupon.isPublished(), result.isPublished());
        assertEquals(existingCoupon.isRedeemed(), result.isRedeemed());
        verify(repositoryPort, times(1)).findByCode(existingCoupon.getCode());
    }

    @Test
    void shouldThrowExceptionWhenCouponNotFound() {
        String code = "NOPE01";
        when(repositoryPort.findByCode(code)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> useCase.execute(code));

        assertEquals("Coupon not found with code: " + code, exception.getMessage());
        verify(repositoryPort, times(1)).findByCode(code);
    }

    @Test
    void shouldGetExpiredCouponSuccessfullyWhenFound() {
        UUID id = UUID.randomUUID();
        Coupon expiredCoupon = Coupon.rehydrate(
                id,
                "EXP111",
                "Expired coupon",
                new BigDecimal("10"),
                OffsetDateTime.now().minusDays(2),
                "ACTIVE",
                true,
                false,
                false,
                null
        );

        when(repositoryPort.findByCode(expiredCoupon.getCode())).thenReturn(Optional.of(expiredCoupon));

        Coupon result = useCase.execute(expiredCoupon.getCode());

        assertNotNull(result);
        assertEquals(expiredCoupon.getId(), result.getId());
        assertEquals(expiredCoupon.getCode(), result.getCode());
        assertEquals(expiredCoupon.getExpirationDate(), result.getExpirationDate());
        verify(repositoryPort, times(1)).findByCode(expiredCoupon.getCode());
    }
}
