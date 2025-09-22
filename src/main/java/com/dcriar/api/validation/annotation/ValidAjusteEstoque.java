package com.dcriar.api.validation.annotation;

import com.dcriar.api.validation.validator.AjusteEstoqueValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AjusteEstoqueValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAjusteEstoque {
    String message() default "Ajuste de estoque inválido.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
