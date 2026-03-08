package br.com.gustavo.coupon.adapters.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import br.com.gustavo.coupon.application.ports.out.CouponRepositoryPort;
import br.com.gustavo.coupon.domain.model.Coupon;

@Component
public class CouponPersistenceAdapter implements CouponRepositoryPort {

    private final CouponJpaRepository jpaRepository;

    public CouponPersistenceAdapter(CouponJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Coupon save(Coupon coupon) {
        CouponEntity entity = toEntity(coupon);

        CouponEntity savedEntity = jpaRepository.saveAndFlush(entity);

        return toDomain(savedEntity);
    }

    @Override
    public Optional<Coupon> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public Optional<Coupon> findByCode(String code) {
        return jpaRepository.findByCode(code)
                .map(this::toDomain);
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
