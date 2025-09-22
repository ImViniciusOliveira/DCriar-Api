package com.dcriar.api.validation.annotation;

import com.dcriar.api.validation.validator.OrdemDeCorteRequestValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = OrdemDeCorteRequestValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidOrdemDeCorteRequest {
    String message() default "Ordem de corte inválida.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
