package com.dcriar.api.validation.annotation;

import com.dcriar.api.validation.validator.TipoMateriaPrimaRequestValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = TipoMateriaPrimaRequestValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTipoMateriaPrimaRequest {
    String message() default "Tipo de matéria-prima inválido.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
