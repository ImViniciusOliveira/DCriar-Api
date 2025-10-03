package com.dcriar.api.validation.annotation;

import com.dcriar.api.dto.request.product.ProdutoRequestDTO;
import com.dcriar.api.validation.validator.ProdutoRequestValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Anotação de validação para garantir que um {@link ProdutoRequestDTO} seja válido.
 * <p>
 * Esta anotação é aplicada no nível da classe e utiliza o {@link ProdutoRequestValidator}
 * para implementar a lógica de validação, que verifica se os campos obrigatórios
 * (como nome, sku, cor, etc.) não são nulos ou vazios.
 *
 * @see ProdutoRequestValidator
 * @see ProdutoRequestDTO
 */
@Documented
@Constraint(validatedBy = ProdutoRequestValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidProdutoRequest {
    String message() default "Requisição de produto inválida.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
