package br.com.gustavo.coupon.application.usecases;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import br.com.gustavo.coupon.application.ports.in.DeleteCouponUseCase;
import br.com.gustavo.coupon.application.ports.out.CouponRepositoryPort;
import br.com.gustavo.coupon.domain.model.Coupon;
import br.com.gustavo.shared.exception.BusinessException;

@Service
public class DeleteCouponUseCaseImpl implements DeleteCouponUseCase {

    private final CouponRepositoryPort repositoryPort;

    public DeleteCouponUseCaseImpl(CouponRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    @Transactional
    public void execute(UUID id) {
        Coupon coupon = repositoryPort.findById(id)
                .orElseThrow(() -> new BusinessException("Coupon not found with id: " + id));

        coupon.delete();

        repositoryPort.save(coupon);
    }
}
