package com.dcriar.api.validation.validator;

import com.dcriar.api.dto.request.product.AjusteEstoqueRequestDTO;
import com.dcriar.api.validation.annotation.ValidAjusteEstoque;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AjusteEstoqueValidator implements ConstraintValidator<ValidAjusteEstoque, AjusteEstoqueRequestDTO> {

    @Override
    public boolean isValid(AjusteEstoqueRequestDTO dto, ConstraintValidatorContext context) {
        if (dto == null) return true;

        boolean valid = true;
        context.disableDefaultConstraintViolation();

        // ProdutoId obrigatório
        if (dto.getProdutoId() == null) {
            context.buildConstraintViolationWithTemplate("O ID do produto é obrigatório.")
                    .addPropertyNode("produtoId").addConstraintViolation();
            valid = false;
        }

        // CanalVendaId obrigatório
        if (dto.getCanalVendaId() == null) {
            context.buildConstraintViolationWithTemplate("O ID do canal de venda é obrigatório.")
                    .addPropertyNode("canalVendaId").addConstraintViolation();
            valid = false;
        }

        // Quantidade não nula e diferente de zero
        if (dto.getQuantidade() == null || dto.getQuantidade() == 0) {
            context.buildConstraintViolationWithTemplate("A quantidade do ajuste deve ser diferente de zero.")
                    .addPropertyNode("quantidade").addConstraintViolation();
            valid = false;
        }

        return valid;
    }
}
