package br.com.gustavo.coupon.application.ports.in;

import br.com.gustavo.coupon.domain.model.Coupon;

public interface CreateCouponUseCase {
    Coupon execute(CreateCouponCommand command);   
}