package com.dcriar.api.validation.validator;

import com.dcriar.api.dto.request.product.ComposicaoRequestDTO;
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

        // Validação da composição
        if (dto.getComposicao() == null || dto.getComposicao().isEmpty()) {
            context.buildConstraintViolationWithTemplate("Todo produto deve ter pelo menos uma composição.")
                    .addPropertyNode("composicao").addConstraintViolation();
            valid = false;
        } else {
            for (int i = 0; i < dto.getComposicao().size(); i++) {
                ComposicaoRequestDTO comp = dto.getComposicao().toArray(new ComposicaoRequestDTO[0])[i];
                if (comp.getMateriaPrimaId() == null) {
                    context.buildConstraintViolationWithTemplate("O ID da matéria-prima é obrigatório.")
                            .addPropertyNode("composicao[" + i + "].materiaPrimaId").addConstraintViolation();
                    valid = false;
                }
                if (comp.getGastoMaterialPorUnidade() == null || comp.getGastoMaterialPorUnidade().doubleValue() <= 0) {
                    context.buildConstraintViolationWithTemplate("O gasto de material deve ser um número positivo.")
                            .addPropertyNode("composicao[" + i + "].gastoMaterialPorUnidade").addConstraintViolation();
                    valid = false;
                }
            }
        }

        return valid;
    }
}
