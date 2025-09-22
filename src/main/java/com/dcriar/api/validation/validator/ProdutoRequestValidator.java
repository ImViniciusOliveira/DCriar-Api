package com.dcriar.api.validation.validator;

import com.dcriar.api.dto.request.product.ProdutoRequestDTO;
import com.dcriar.api.validation.annotation.ValidProdutoRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ProdutoRequestValidator implements ConstraintValidator<ValidProdutoRequest, ProdutoRequestDTO> {

    @Override
    public boolean isValid(ProdutoRequestDTO dto, ConstraintValidatorContext context) {
        if (dto == null) return true;

        boolean valid = true;
        context.disableDefaultConstraintViolation();

        if (dto.getNome() == null || dto.getNome().isBlank()) {
            context.buildConstraintViolationWithTemplate("O nome do produto é obrigatório.")
                    .addPropertyNode("nome").addConstraintViolation();
            valid = false;
        }

        if (dto.getSku() == null || dto.getSku().isBlank()) {
            context.buildConstraintViolationWithTemplate("O SKU do produto é obrigatório.")
                    .addPropertyNode("sku").addConstraintViolation();
            valid = false;
        }

        if (dto.getCor() == null || dto.getCor().isBlank()) {
            context.buildConstraintViolationWithTemplate("A cor do produto é obrigatória.")
                    .addPropertyNode("cor").addConstraintViolation();
            valid = false;
        }

        if (dto.getUnidadesPorProduto() == null || dto.getUnidadesPorProduto() <= 0) {
            context.buildConstraintViolationWithTemplate("A quantidade de unidades por produto deve ser positiva.")
                    .addPropertyNode("unidadesPorProduto").addConstraintViolation();
            valid = false;
        }

        if (dto.getComposicao() != null) {
            dto.getComposicao().forEach(comp -> {
                if (comp.materiaPrimaId() == null) {
                    context.buildConstraintViolationWithTemplate("O ID da matéria-prima é obrigatório.")
                            .addPropertyNode("composicao.materiaPrimaId").addConstraintViolation();
                }
                if (comp.gastoMaterialPorUnidade() == null || comp.gastoMaterialPorUnidade().doubleValue() <= 0) {
                    context.buildConstraintViolationWithTemplate("O gasto de material deve ser um número positivo.")
                            .addPropertyNode("composicao.gastoMaterialPorUnidade").addConstraintViolation();
                }
            });
        }

        return valid;
    }
}
