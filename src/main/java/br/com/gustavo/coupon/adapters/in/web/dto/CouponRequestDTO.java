package br.com.gustavo.coupon.adapters.in.web.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CouponRequestDTO(
        @NotBlank(message = "The code is required")
        String code,

        @NotBlank(message = "The description is required")
        String description,

        @NotNull(message = "The discount value is required")
        @Min(value = 0, message = "The discount value cannot be negative")
        BigDecimal discountValue,

        @NotNull(message = "The expiration date is required")
        @FutureOrPresent(message = "The expiration date cannot be in the past")
        OffsetDateTime expirationDate,

        boolean published
) {}
