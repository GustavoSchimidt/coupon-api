package br.com.gustavo.coupon.adapters.in.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gustavo.coupon.adapters.in.web.dto.CouponRequestDTO;
import br.com.gustavo.coupon.adapters.in.web.dto.CouponResponseDTO;
import br.com.gustavo.coupon.application.ports.in.CreateCouponCommand;
import br.com.gustavo.coupon.application.ports.in.CreateCouponUseCase;
import br.com.gustavo.coupon.application.ports.in.DeleteCouponUseCase;
import br.com.gustavo.coupon.application.ports.in.GetCouponUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/coupon")
@Tag(name = "Coupons", description = "Endpoints para gerenciamento de cupons de desconto")
public class CouponController {

    private final CreateCouponUseCase createCouponUseCase;
    private final DeleteCouponUseCase deleteCouponUseCase;
    private final GetCouponUseCase getCouponUseCase;

    public CouponController(CreateCouponUseCase createCouponUseCase, DeleteCouponUseCase deleteCouponUseCase, GetCouponUseCase getCouponUseCase) {
        this.createCouponUseCase = createCouponUseCase;
        this.deleteCouponUseCase = deleteCouponUseCase;
        this.getCouponUseCase = getCouponUseCase;
    }

    @GetMapping("/{code}")
    @Operation(summary = "Retorna um cupom", description = "Retorna um cupom pelo código")
    public ResponseEntity<CouponResponseDTO> get(@PathVariable String code) {
        
        CouponResponseDTO coupon = getCouponUseCase.execute(code);
        
        return ResponseEntity.status(HttpStatus.OK).body(coupon);
    }

    @PostMapping
    @Operation(summary = "Cria um novo cupom", description = "Valida as regras de negócio e cadastra um novo cupom.")
    public ResponseEntity<CouponResponseDTO> create(@Valid @RequestBody CouponRequestDTO request) {

        CreateCouponCommand command = new CreateCouponCommand(
                request.code(),
                request.description(),
                request.discountValue(),
                request.expirationDate(),
                request.published()
        );

        CouponResponseDTO coupon = createCouponUseCase.execute(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(coupon);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deleta um cupom", description = "Realiza o soft delete de um cupom existente.")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        
        deleteCouponUseCase.execute(id);
        
        return ResponseEntity.noContent().build();
    }
}
