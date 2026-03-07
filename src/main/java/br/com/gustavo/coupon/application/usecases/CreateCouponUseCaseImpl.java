package br.com.gustavo.coupon.application.usecases;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gustavo.coupon.application.ports.in.CreateCouponCommand;
import br.com.gustavo.coupon.application.ports.in.CreateCouponUseCase;
import br.com.gustavo.coupon.application.ports.out.CouponRepositoryPort;
import br.com.gustavo.coupon.domain.model.Coupon;

@Service
public class CreateCouponUseCaseImpl implements CreateCouponUseCase {

    private final CouponRepositoryPort repositoryPort;

    public CreateCouponUseCaseImpl(CouponRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    @Transactional
    public Coupon execute(CreateCouponCommand command) {
        Coupon coupon = Coupon.create(
                command.code(),
                command.description(),
                command.discountValue(),
                command.expirationDate(),
                command.published()
        );

        return repositoryPort.save(coupon);
    }
}