package br.com.gustavo.coupon.adapters.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponJpaRepository extends JpaRepository<CouponEntity, UUID> {
    boolean existsByCode(String code);
    Optional<CouponEntity> findByCode(String code);
}
