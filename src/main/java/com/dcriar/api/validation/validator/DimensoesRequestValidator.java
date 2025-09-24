package com.dcriar.api.validation.validator;

import com.dcriar.api.dto.request.product.DimensoesRequestDTO;
import com.dcriar.api.validation.annotation.ValidDimensoesRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class DimensoesRequestValidator implements ConstraintValidator<ValidDimensoesRequest, DimensoesRequestDTO> {

    @Override
    public boolean isValid(DimensoesRequestDTO dto, ConstraintValidatorContext context) {
        if (dto == null) return true;

        boolean valid = true;
        context.disableDefaultConstraintViolation();

        // Validação da largura
        if (dto.getLargura() == null) {
            context.buildConstraintViolationWithTemplate("A largura é obrigatória.")
                    .addPropertyNode("largura").addConstraintViolation();
            valid = false;
        } else if (dto.getLargura().compareTo(BigDecimal.ZERO) <= 0) {
            context.buildConstraintViolationWithTemplate("A largura deve ser positiva.")
                    .addPropertyNode("largura").addConstraintViolation();
            valid = false;
        }

        // Validação do comprimento
        if (dto.getComprimento() == null) {
            context.buildConstraintViolationWithTemplate("O comprimento é obrigatório.")
                    .addPropertyNode("comprimento").addConstraintViolation();
            valid = false;
        } else if (dto.getComprimento().compareTo(BigDecimal.ZERO) <= 0) {
            context.buildConstraintViolationWithTemplate("O comprimento deve ser positivo.")
                    .addPropertyNode("comprimento").addConstraintViolation();
            valid = false;
        }

        return valid;
    }
}
