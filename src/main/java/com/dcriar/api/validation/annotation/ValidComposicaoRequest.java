package com.dcriar.api.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = com.dcriar.api.validation.validator.ComposicaoRequestValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidComposicaoRequest {
    String message() default "Composição inválida";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
