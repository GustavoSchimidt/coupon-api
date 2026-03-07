package br.com.gustavo.coupon.application.ports.out;

import java.util.Optional;
import java.util.UUID;

import br.com.gustavo.coupon.domain.model.Coupon;

public interface CouponRepositoryPort {
    Coupon save(Coupon coupon);
    Optional<Coupon> findById(UUID id);
}
