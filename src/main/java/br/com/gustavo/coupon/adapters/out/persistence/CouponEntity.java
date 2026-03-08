package br.com.gustavo.coupon.adapters.out.persistence;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "coupons")
@SQLRestriction("deleted = false")
public class CouponEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 6)
    private String code;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(nullable = false)
    private OffsetDateTime expirationDate;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private boolean published;

    @Column(nullable = false)
    private boolean redeemed;

    @Column(nullable = false)
    private boolean deleted;

    private OffsetDateTime deletedAt;

    public CouponEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
    public OffsetDateTime getExpirationDate() { return expirationDate; }
    public void setExpirationDate(OffsetDateTime expirationDate) { this.expirationDate = expirationDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }
    public boolean isRedeemed() { return redeemed; }
    public void setRedeemed(boolean redeemed) { this.redeemed = redeemed; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    public OffsetDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(OffsetDateTime deletedAt) { this.deletedAt = deletedAt; }
}
