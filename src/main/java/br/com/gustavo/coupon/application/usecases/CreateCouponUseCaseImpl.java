package br.com.gustavo.coupon.application.usecases;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gustavo.coupon.adapters.in.web.dto.CouponResponseDTO;
import br.com.gustavo.coupon.application.ports.in.CreateCouponCommand;
import br.com.gustavo.coupon.application.ports.in.CreateCouponUseCase;
import br.com.gustavo.coupon.application.ports.out.CouponRepositoryPort;
import br.com.gustavo.coupon.domain.model.Coupon;
import br.com.gustavo.shared.exception.BusinessException;

@Service
public class CreateCouponUseCaseImpl implements CreateCouponUseCase {

    private final CouponRepositoryPort repositoryPort;

    public CreateCouponUseCaseImpl(CouponRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    @Transactional
    public CouponResponseDTO execute(CreateCouponCommand command) {
        Coupon coupon = Coupon.create(
                command.code(),
                command.description(),
                command.discountValue(),
                command.expirationDate(),
                command.published()
        );

        this.couponExists(coupon.getCode());

        Coupon couponCreated = repositoryPort.save(coupon);

        return new CouponResponseDTO(
            couponCreated.getId(),
            couponCreated.getCode(),
            couponCreated.getDescription(),
            couponCreated.getDiscountValue(),
            couponCreated.getExpirationDate(),
            couponCreated.getStatus(),
            couponCreated.isPublished(),
            couponCreated.isRedeemed()
        );
    }

    private void couponExists(String code) {
        Optional<Coupon> couponExists = repositoryPort.findByCode(code);

        if (couponExists.isPresent()) {
            throw new BusinessException(400, "Coupon already exists with code: " + code);
        }
    }
}