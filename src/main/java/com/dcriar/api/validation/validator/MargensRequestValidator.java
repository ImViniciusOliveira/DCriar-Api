package com.dcriar.api.validation.validator;

import com.dcriar.api.dto.request.production.MargensRequestDTO;
import com.dcriar.api.validation.annotation.ValidMargensRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class MargensRequestValidator implements ConstraintValidator<ValidMargensRequest, MargensRequestDTO> {

    @Override
    public boolean isValid(MargensRequestDTO value, ConstraintValidatorContext context) {
        if (value == null) {
            // Se margens não forem obrigatórias, podemos aceitar null
            return true;
        }

        return isNullOrPositive(value.getSuperior())
                && isNullOrPositive(value.getInferior())
                && isNullOrPositive(value.getEsquerda())
                && isNullOrPositive(value.getDireita());
    }

    private boolean isNullOrPositive(BigDecimal number) {
        return number == null || number.compareTo(BigDecimal.ZERO) >= 0;
    }
}
