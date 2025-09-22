package com.dcriar.api.validation.annotation;

import com.dcriar.api.validation.validator.SaleItemRequestValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SaleItemRequestValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSaleItemRequest {
    String message() default "Item de venda inválido.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
