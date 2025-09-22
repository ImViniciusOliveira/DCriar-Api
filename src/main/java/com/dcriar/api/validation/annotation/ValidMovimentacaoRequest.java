package com.dcriar.api.validation.annotation;

import com.dcriar.api.validation.validator.MovimentacaoRequestValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MovimentacaoRequestValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidMovimentacaoRequest {
    String message() default "Movimentação inválida.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
