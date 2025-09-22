package com.dcriar.api.validation.annotation;

import com.dcriar.api.validation.validator.ProdutoRequestValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ProdutoRequestValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidProdutoRequest {
    String message() default "Produto inválido.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
