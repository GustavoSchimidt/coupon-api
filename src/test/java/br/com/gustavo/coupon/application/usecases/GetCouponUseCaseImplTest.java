package br.com.gustavo.coupon.application.usecases;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        assertEquals(existingCoupon.getId(), result.id());
        assertEquals(existingCoupon.getCode(), result.code());
        assertEquals(existingCoupon.getDescription(), result.description());
        assertEquals(existingCoupon.getDiscountValue(), result.discountValue());
        assertEquals(existingCoupon.getExpirationDate(), result.expirationDate());
        assertEquals(existingCoupon.getStatus(), result.status());
        assertEquals(existingCoupon.isPublished(), result.published());
        assertEquals(existingCoupon.isRedeemed(), result.redeemed());
        verify(repositoryPort, times(1)).findByCode(existingCoupon.getCode());
    }
}
