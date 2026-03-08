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
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.gustavo.coupon.application.ports.in.CreateCouponCommand;
import br.com.gustavo.coupon.application.ports.out.CouponRepositoryPort;
import br.com.gustavo.coupon.domain.model.Coupon;
import br.com.gustavo.shared.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class CreateCouponUseCaseImplTest {

    @Mock
    private CouponRepositoryPort repositoryPort;

    @InjectMocks
    private CreateCouponUseCaseImpl useCase;

    @Test
    void shouldOrchestrateCouponCreationAndSave() {
        CreateCouponCommand command = new CreateCouponCommand(
                "TEST10",
                "Cupom de Teste",
                new BigDecimal("10.0"),
                OffsetDateTime.now().plusDays(5),
                true
        );

        when(repositoryPort.findByCode(command.code())).thenReturn(java.util.Optional.empty());

        when(repositoryPort.save(any(Coupon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Coupon savedCoupon = useCase.execute(command);

        assertNotNull(savedCoupon);
        assertEquals("TEST10", savedCoupon.getCode());
        verify(repositoryPort, times(1)).save(any(Coupon.class));
    }

    @Test
    void shouldThrowExceptionWhenCouponAlreadyExists() {
        CreateCouponCommand command = new CreateCouponCommand(
                "TEST10",
                "Cupom de Teste",
                new BigDecimal("10.0"),
                OffsetDateTime.now().plusDays(5),
                true
        );
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
        
        when(repositoryPort.findByCode("TEST10")).thenReturn(Optional.of(existingCoupon));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> useCase.execute(command)
        );

        assertEquals("Coupon already exists with code: TEST10", exception.getMessage());
        verify(repositoryPort, never()).save(any(Coupon.class));
    }
}
