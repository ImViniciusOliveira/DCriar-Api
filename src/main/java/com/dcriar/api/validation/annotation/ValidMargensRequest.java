package com.dcriar.api.validation.annotation;

import com.dcriar.api.validation.validator.MargensRequestValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MargensRequestValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidMargensRequest {

    String message() default "Margens inválidas: valores devem ser nulos ou maiores/iguais a zero.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
