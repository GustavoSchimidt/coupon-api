package br.com.gustavo.coupon.application.ports.in;

import br.com.gustavo.coupon.adapters.in.web.dto.CouponResponseDTO;

public interface CreateCouponUseCase {
    CouponResponseDTO execute(CreateCouponCommand command);   
}