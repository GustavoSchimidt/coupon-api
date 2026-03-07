package br.com.gustavo.coupon.application.usecases;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.gustavo.coupon.application.ports.out.CouponRepositoryPort;
import br.com.gustavo.coupon.domain.model.Coupon;
import br.com.gustavo.shared.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class DeleteCouponUseCaseImplTest {

    @Mock
    private CouponRepositoryPort repositoryPort;

    @InjectMocks
    private DeleteCouponUseCaseImpl useCase;

    @Test
    void shouldDeleteCouponSuccessfullyWhenFound() {
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
        
        when(repositoryPort.findById(id)).thenReturn(Optional.of(existingCoupon));

        useCase.execute(id);

        assertTrue(existingCoupon.isDeleted());
        verify(repositoryPort, times(1)).save(existingCoupon);
    }

    @Test
    void shouldThrowExceptionWhenCouponNotFoundForDeletion() {
        UUID id = UUID.randomUUID();
        
        when(repositoryPort.findById(id)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> useCase.execute(id));
        
        assertEquals("Coupon not found with id: " + id, exception.getMessage());
        
        verify(repositoryPort, never()).save(any());
    }
}
