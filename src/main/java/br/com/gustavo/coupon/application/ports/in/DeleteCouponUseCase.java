package br.com.gustavo.coupon.application.ports.in; 

import java.util.UUID;

public interface DeleteCouponUseCase {
    void execute(UUID id);
}
