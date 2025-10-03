package com.dcriar.api.validation.annotation;

import com.dcriar.api.dto.request.sales.SaleItemRequestDTO;
import com.dcriar.api.validation.validator.SaleItemRequestValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Anotação de validação para garantir que um {@link SaleItemRequestDTO} seja válido.
 * <p>
 * Esta anotação é aplicada no nível da classe e utiliza o {@link SaleItemRequestValidator}
 * para implementar a lógica de validação, que verifica se o ID do produto não é nulo
 * e se a quantidade é um valor positivo.
 *
 * @see SaleItemRequestValidator
 * @see SaleItemRequestDTO
 */
@Documented
@Constraint(validatedBy = SaleItemRequestValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSaleItemRequest {
    String message() default "Item de venda inválido.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
