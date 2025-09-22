package com.dcriar.api.validation.annotation;

import com.dcriar.api.validation.validator.AjusteEstoqueProdutoValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AjusteEstoqueProdutoValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAjusteEstoqueProduto {
    String message() default "Ajuste de estoque de produto inválido.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
