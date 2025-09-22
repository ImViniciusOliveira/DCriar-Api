package com.dcriar.api.validation.validator;

import com.dcriar.api.dto.request.stock.TipoMateriaPrimaRequestDTO;
import com.dcriar.api.validation.annotation.ValidTipoMateriaPrimaRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TipoMateriaPrimaRequestValidator implements ConstraintValidator<ValidTipoMateriaPrimaRequest, TipoMateriaPrimaRequestDTO> {

    @Override
    public boolean isValid(TipoMateriaPrimaRequestDTO dto, ConstraintValidatorContext context) {
        if (dto == null) return true;

        boolean valid = true;
        context.disableDefaultConstraintViolation();

        if (dto.nome() == null || dto.nome().isBlank()) {
            context.buildConstraintViolationWithTemplate("O nome do tipo de matéria-prima é obrigatório.")
                    .addPropertyNode("nome").addConstraintViolation();
            valid = false;
        } else if (dto.nome().length() > 150) {
            context.buildConstraintViolationWithTemplate("O nome não pode ter mais de 150 caracteres.")
                    .addPropertyNode("nome").addConstraintViolation();
            valid = false;
        }

        if (dto.unidadeDeConsumo() == null) {
            context.buildConstraintViolationWithTemplate("A unidade de consumo é obrigatória.")
                    .addPropertyNode("unidadeDeConsumo").addConstraintViolation();
            valid = false;
        }

        return valid;
    }
}
