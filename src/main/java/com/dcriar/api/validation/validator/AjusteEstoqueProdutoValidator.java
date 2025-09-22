package com.dcriar.api.validation.validator;

import com.dcriar.api.dto.request.product.AjusteEstoqueProdutoRequestDTO;
import com.dcriar.api.validation.annotation.ValidAjusteEstoqueProduto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AjusteEstoqueProdutoValidator implements ConstraintValidator<ValidAjusteEstoqueProduto, AjusteEstoqueProdutoRequestDTO> {

    @Override
    public boolean isValid(AjusteEstoqueProdutoRequestDTO dto, ConstraintValidatorContext context) {
        if (dto == null) return true;

        boolean valid = true;
        context.disableDefaultConstraintViolation();

        // ProdutoId obrigatório
        if (dto.getProdutoId() == null) {
            context.buildConstraintViolationWithTemplate("O ID do produto é obrigatório.")
                    .addPropertyNode("produtoId").addConstraintViolation();
            valid = false;
        }

        // Quantidade não nula e não zero
        if (dto.getQuantidade() == null || dto.getQuantidade() == 0) {
            context.buildConstraintViolationWithTemplate("A quantidade do ajuste deve ser diferente de zero.")
                    .addPropertyNode("quantidade").addConstraintViolation();
            valid = false;
        }

        // Motivo obrigatório
        if (dto.getMotivo() == null || dto.getMotivo().isBlank()) {
            context.buildConstraintViolationWithTemplate("O motivo é obrigatório para ajustes manuais.")
                    .addPropertyNode("motivo").addConstraintViolation();
            valid = false;
        }

        return valid;
    }
}
