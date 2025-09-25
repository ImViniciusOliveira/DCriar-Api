package com.dcriar.api.validation.validator;

import com.dcriar.api.dto.request.product.DimensoesRequestDTO;
import com.dcriar.api.validation.annotation.ValidDimensoesRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class DimensoesRequestValidator implements ConstraintValidator<ValidDimensoesRequest, DimensoesRequestDTO> {

    @Override
    public boolean isValid(DimensoesRequestDTO value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        BigDecimal largura = value.getLarguraCm();
        BigDecimal comprimento = value.getComprimentoCm();

        boolean valid = true;

        if (largura == null || largura.compareTo(BigDecimal.ZERO) <= 0) {
            addConstraintViolation(context, "A largura deve ser informada e maior que zero.");
            valid = false;
        }

        if (comprimento == null || comprimento.compareTo(BigDecimal.ZERO) <= 0) {
            addConstraintViolation(context, "O comprimento deve ser informado e maior que zero.");
            valid = false;
        }

        return valid;
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode("dimensoes")
                .addConstraintViolation();
    }
}