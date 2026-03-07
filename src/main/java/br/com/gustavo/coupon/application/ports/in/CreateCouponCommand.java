package br.com.gustavo.coupon.application.ports.in;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CreateCouponCommand(
        String code,
        String description,
        BigDecimal discountValue,
        OffsetDateTime expirationDate,
        boolean published
) {}
