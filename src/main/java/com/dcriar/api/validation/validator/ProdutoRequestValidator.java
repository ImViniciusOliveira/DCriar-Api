package com.dcriar.api.validation.validator;

import com.dcriar.api.dto.request.product.DimensoesRequestDTO;
import com.dcriar.api.dto.request.product.ProdutoRequestDTO;
import com.dcriar.api.validation.annotation.ValidProdutoRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class ProdutoRequestValidator implements ConstraintValidator<ValidProdutoRequest, ProdutoRequestDTO> {

    @Override
    public boolean isValid(ProdutoRequestDTO dto, ConstraintValidatorContext context) {
        if (dto == null) return true;

        boolean valid = true;
        context.disableDefaultConstraintViolation();

        // Validação do nome
        if (dto.getNome() == null || dto.getNome().isBlank()) {
            context.buildConstraintViolationWithTemplate("O nome do produto é obrigatório.")
                    .addPropertyNode("nome").addConstraintViolation();
            valid = false;
        }

        // Validação do SKU
        if (dto.getSku() == null || dto.getSku().isBlank()) {
            context.buildConstraintViolationWithTemplate("O SKU do produto é obrigatório.")
                    .addPropertyNode("sku").addConstraintViolation();
            valid = false;
        }

        // Validação da cor
        if (dto.getCor() == null || dto.getCor().isBlank()) {
            context.buildConstraintViolationWithTemplate("A cor do produto é obrigatória.")
                    .addPropertyNode("cor").addConstraintViolation();
            valid = false;
        }

        // Validação das unidades por produto
        if (dto.getUnidadesPorProduto() == null || dto.getUnidadesPorProduto() <= 0) {
            context.buildConstraintViolationWithTemplate("A quantidade de unidades por produto deve ser positiva.")
                    .addPropertyNode("unidadesPorProduto").addConstraintViolation();
            valid = false;
        }

        // Validação do tipo de matéria-prima
        if (dto.getTipoMateriaPrimaId() == null) {
            context.buildConstraintViolationWithTemplate("O ID do tipo de matéria-prima é obrigatório.")
                    .addPropertyNode("tipoMateriaPrimaId").addConstraintViolation();
            valid = false;
        }

        // Validação das dimensões unitárias
        DimensoesRequestDTO dim = dto.getDimensoesUnitarias();
        if (dim == null) {
            context.buildConstraintViolationWithTemplate("As dimensões unitárias são obrigatórias.")
                    .addPropertyNode("dimensoesUnitarias").addConstraintViolation();
            valid = false;
        } else {
            if (dim.getLargura() == null || dim.getLargura().compareTo(BigDecimal.ZERO) <= 0) {
                context.buildConstraintViolationWithTemplate("A largura deve ser positiva.")
                        .addPropertyNode("dimensoesUnitarias.largura").addConstraintViolation();
                valid = false;
            }
            if (dim.getComprimento() == null || dim.getComprimento().compareTo(BigDecimal.ZERO) <= 0) {
                context.buildConstraintViolationWithTemplate("O comprimento deve ser positivo.")
                        .addPropertyNode("dimensoesUnitarias.comprimento").addConstraintViolation();
                valid = false;
            }
        }

        return valid;
    }
}
