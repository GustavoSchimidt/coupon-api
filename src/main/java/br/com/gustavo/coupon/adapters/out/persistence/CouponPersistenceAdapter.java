package br.com.gustavo.coupon.adapters.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import br.com.gustavo.coupon.application.ports.out.CouponRepositoryPort;
import br.com.gustavo.coupon.domain.model.Coupon;
import br.com.gustavo.shared.exception.BusinessException;

@Component
public class CouponPersistenceAdapter implements CouponRepositoryPort {

    private final CouponJpaRepository jpaRepository;

    public CouponPersistenceAdapter(CouponJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Coupon save(Coupon coupon) {
        CouponEntity entity = toEntity(coupon);
        boolean creation = coupon.getId() == null;

        if (creation) {
            ensureCodeIsAvailable(coupon.getCode());
        } else {
            attachIdFromActiveCoupon(coupon.getId(), entity);
        }

        CouponEntity savedEntity;
        try {
            savedEntity = jpaRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException ex) {
            if (creation) {
                throw new BusinessException("Coupon already exists with code: " + coupon.getCode());
            }
            throw ex;
        }

        return toDomain(savedEntity);
    }

    @Override
    public Optional<Coupon> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(this::toDomain);
    }

    private void ensureCodeIsAvailable(String code) {
        if (jpaRepository.existsByCode(code)) {
            throw new BusinessException("Coupon already exists with code: " + code);
        }
    }

    private void attachIdFromActiveCoupon(UUID id, CouponEntity entity) {
        CouponEntity existing = jpaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Coupon not found with id: " + id));
        entity.setId(existing.getId());
    }
    
    private CouponEntity toEntity(Coupon coupon) {
        CouponEntity entity = new CouponEntity();
        entity.setId(coupon.getId());
        entity.setCode(coupon.getCode());
        entity.setDescription(coupon.getDescription());
        entity.setDiscountValue(coupon.getDiscountValue());
        entity.setExpirationDate(coupon.getExpirationDate());
        entity.setStatus(coupon.getStatus());
        entity.setPublished(coupon.isPublished());
        entity.setRedeemed(coupon.isRedeemed());
        entity.setDeleted(coupon.isDeleted());
        entity.setDeletedAt(coupon.getDeletedAt());
        return entity;
    }

    private Coupon toDomain(CouponEntity entity) {
        return Coupon.rehydrate(
                entity.getId(),
                entity.getCode(),
                entity.getDescription(),
                entity.getDiscountValue(),
                entity.getExpirationDate(),
                entity.getStatus(),
                entity.isPublished(),
                entity.isRedeemed(),
                entity.isDeleted(),
                entity.getDeletedAt()
        );
    }
}
