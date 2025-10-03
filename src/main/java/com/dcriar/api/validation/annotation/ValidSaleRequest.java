package com.dcriar.api.validation.annotation;

import com.dcriar.api.dto.request.sales.SaleRequestDTO;
import com.dcriar.api.validation.validator.SaleRequestValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Anotação de validação para garantir que um {@link SaleRequestDTO} seja válido.
 * <p>
 * Esta anotação é aplicada no nível da classe e utiliza o {@link SaleRequestValidator}
 * para implementar a lógica de validação, que verifica se os campos obrigatórios
 * (como canalVendaId e a lista de itens) não são nulos ou vazios.
 *
 * @see SaleRequestValidator
 * @see SaleRequestDTO
 */
@Documented
@Constraint(validatedBy = SaleRequestValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSaleRequest {
    String message() default "Requisição de venda inválida.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
