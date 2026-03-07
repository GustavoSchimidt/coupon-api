package br.com.gustavo.coupon.application.usecases;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.gustavo.coupon.application.ports.in.CreateCouponCommand;
import br.com.gustavo.coupon.application.ports.out.CouponRepositoryPort;
import br.com.gustavo.coupon.domain.model.Coupon;

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

        when(repositoryPort.save(any(Coupon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Coupon savedCoupon = useCase.execute(command);

        assertNotNull(savedCoupon);
        assertEquals("TEST10", savedCoupon.getCode());
        verify(repositoryPort, times(1)).save(any(Coupon.class));
    }
}
