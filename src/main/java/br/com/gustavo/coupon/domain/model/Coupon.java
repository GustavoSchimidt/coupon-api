package br.com.gustavo.coupon.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import br.com.gustavo.shared.exception.BusinessException;

public class Coupon {
    private static final String ACTIVE_STATUS = "ACTIVE";

    private final UUID id;
    private final String code;
    private final String description;
    private final BigDecimal discountValue;
    private final OffsetDateTime expirationDate;
    private final String status;
    private final boolean published;
    private final boolean redeemed;
    private boolean deleted;
    private OffsetDateTime deletedAt;

    private Coupon(UUID id, String code, String description, BigDecimal discountValue, OffsetDateTime expirationDate,
                   String status, boolean published, boolean redeemed, boolean deleted, OffsetDateTime deletedAt) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.discountValue = discountValue;
        this.expirationDate = expirationDate;
        this.status = status;
        this.published = published;
        this.redeemed = redeemed;
        this.deleted = deleted;
        this.deletedAt = deletedAt;
    }

    public static Coupon create(String code, String description, BigDecimal discountValue, OffsetDateTime expirationDate,
                                boolean published) {
        validateExpirationDate(expirationDate);
        validateDiscountValue(discountValue);
        String sanitizedCode = sanitizeCode(code);
        
        return new Coupon(
                null,
                sanitizedCode,
                description,
                discountValue,
                expirationDate,
                ACTIVE_STATUS,
                published,
                false,
                false,
                null
        );
    }

    public static Coupon rehydrate(
            UUID id,
            String code,
            String description,
            BigDecimal discountValue,
            OffsetDateTime expirationDate,
            String status,
            boolean published,
            boolean redeemed,
            boolean deleted,
            OffsetDateTime deletedAt
    ) {
        validateExpirationDate(expirationDate);
        validateDiscountValue(discountValue);
        String sanitizedCode = sanitizeCode(code);

        OffsetDateTime restoredDeletedAt = deleted ? deletedAt : null;
        String restoredStatus = status == null ? ACTIVE_STATUS : status;

        return new Coupon(
                id,
                sanitizedCode,
                description,
                discountValue,
                expirationDate,
                restoredStatus,
                published,
                redeemed,
                deleted,
                restoredDeletedAt
        );
    }

    private static String sanitizeCode(String code) {
        if (code == null) throw new BusinessException("Code cannot be null");
        
        String sanitized = code.replaceAll("[^a-zA-Z0-9]", "");
        
        if (sanitized.length() != 6) {
           throw new BusinessException("Code must have exactly 6 alphanumeric characters after sanitization");
        }
        return sanitized;
    }

    private static void validateExpirationDate(OffsetDateTime expirationDate) {
        if (expirationDate == null || expirationDate.isBefore(OffsetDateTime.now())) {
            throw new BusinessException("Expiration date cannot be in the past");
        }
    }

    private static void validateDiscountValue(BigDecimal discountValue) {
        if (discountValue == null || discountValue.compareTo(new BigDecimal("0.5")) < 0) {
            throw new BusinessException("Discount value must be at least 0.5");
        }
    }

    public void delete() {
        if (this.deleted) {
            throw new BusinessException("Coupon already deleted"); 
        }
        this.deleted = true;
        this.deletedAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getDescription() { return description; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public OffsetDateTime getExpirationDate() { return expirationDate; }
    public String getStatus() { return status; }
    public boolean isPublished() { return published; }
    public boolean isRedeemed() { return redeemed; }
    public boolean isDeleted() { return deleted; }
    public OffsetDateTime getDeletedAt() { return deletedAt; }
}
