package br.com.gustavo.coupon.adapters.in.web.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CouponResponseDTO(
        UUID id,
        String code,
        String description,
        BigDecimal discountValue,
        OffsetDateTime expirationDate,
        String status,
        boolean published,
        boolean redeemed
) {}
