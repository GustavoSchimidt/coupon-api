package br.com.gustavo.coupon.application.usecases;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gustavo.coupon.adapters.in.web.dto.CouponResponseDTO;
import br.com.gustavo.coupon.application.ports.in.GetCouponUseCase;
import br.com.gustavo.coupon.application.ports.out.CouponRepositoryPort;
import br.com.gustavo.coupon.domain.model.Coupon;
import br.com.gustavo.shared.exception.BusinessException;

@Service
public class GetCouponUseCaseImpl implements GetCouponUseCase {

    private final CouponRepositoryPort repositoryPort;

    public GetCouponUseCaseImpl(CouponRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    @Transactional
    public CouponResponseDTO execute(String code) {
        Coupon coupon = repositoryPort.findByCode(code)
                .orElseThrow(() -> new BusinessException("Coupon not found with code: " + code));

        return new CouponResponseDTO(
            coupon.getId(),
            coupon.getCode(),
            coupon.getDescription(),
            coupon.getDiscountValue(),
            coupon.getExpirationDate(),
            coupon.getStatus(),
            coupon.isPublished(),
            coupon.isRedeemed()
        );
    }
}
