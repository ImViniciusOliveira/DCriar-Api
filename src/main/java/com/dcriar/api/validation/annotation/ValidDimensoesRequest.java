package com.dcriar.api.validation.annotation;

import com.dcriar.api.validation.validator.DimensoesRequestValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DimensoesRequestValidator.class)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDimensoesRequest {
    String message() default "Dimensões inválidas";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
