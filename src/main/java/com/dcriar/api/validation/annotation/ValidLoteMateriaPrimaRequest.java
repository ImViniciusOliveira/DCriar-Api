package com.dcriar.api.validation.annotation;

import com.dcriar.api.validation.validator.LoteMateriaPrimaRequestValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = LoteMateriaPrimaRequestValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidLoteMateriaPrimaRequest {
    String message() default "Lote de matéria-prima inválido.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
