package com.dcriar.api.validation.annotation;

import com.dcriar.api.validation.validator.OrdemDeCorteRequestValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = OrdemDeCorteRequestValidator.class)
@Target({TYPE})
@Retention(RUNTIME)
public @interface ValidOrdemDeCorteRequest {

    String message() default "Ordem de corte inválida: verifique margens e dimensões conforme o modo de cálculo.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
