package com.dcriar.api.validation.validator;

import com.dcriar.api.dto.request.production.OrdemDeCorteRequestDTO;
import com.dcriar.api.dto.request.product.DimensoesRequestDTO;
import com.dcriar.api.dto.request.production.MargensRequestDTO;
import com.dcriar.api.validation.annotation.ValidOrdemDeCorteRequest;
import com.dcriar.domain.production.enums.ModoCalculo;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class OrdemDeCorteRequestValidator implements ConstraintValidator<ValidOrdemDeCorteRequest, OrdemDeCorteRequestDTO> {

    @Override
    public boolean isValid(OrdemDeCorteRequestDTO value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        boolean valid = true;

        if (value.getModoCalculo() == ModoCalculo.AUTOMATICO) {
            MargensRequestDTO margens = value.getMargens();
            if (margens == null) {
                addConstraintViolation(context, "As margens devem ser informadas no modo AUTOMATICO.", "margens");
                valid = false;
            }
        }

        if (value.getModoCalculo() == ModoCalculo.MANUAL) {
            DimensoesRequestDTO dimensoes = value.getTamanhoFinal();
            if (dimensoes == null) {
                addConstraintViolation(context, "As dimensões finais devem ser informadas no modo MANUAL.", "tamanhoFinal");
                valid = false;
            }
        }

        return valid;
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String message, String propertyNode) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(propertyNode)
                .addConstraintViolation();
    }
}