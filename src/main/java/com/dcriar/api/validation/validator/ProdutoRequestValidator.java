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

        // ... (validações de nome, sku, cor - estão perfeitas)

        // Validação das unidades por produto (COM MELHORIA NA MENSAGEM)
        if (dto.getUnidadesPorProduto() == null || dto.getUnidadesPorProduto() <= 0) {
            // MUDANÇA AQUI: Adicionamos o valor recebido à mensagem
            String message = "A quantidade de unidades por produto deve ser positiva. Valor recebido: " + dto.getUnidadesPorProduto();
            context.buildConstraintViolationWithTemplate(message)
                    .addPropertyNode("unidadesPorProduto").addConstraintViolation();
            valid = false;
        }

        // Validação do tipo de matéria-prima (está perfeita)
        if (dto.getTipoMateriaPrimaId() == null) {
            context.buildConstraintViolationWithTemplate("O ID do tipo de matéria-prima é obrigatório.")
                    .addPropertyNode("tipoMateriaPrimaId").addConstraintViolation();
            valid = false;
        }

        // Validação das dimensões unitárias (COM MELHORIA NA MENSAGEM)
        DimensoesRequestDTO dim = dto.getDimensoesUnitarias();
        if (dim == null) {
            context.buildConstraintViolationWithTemplate("As dimensões unitárias são obrigatórias.")
                    .addPropertyNode("dimensoesUnitarias").addConstraintViolation();
            valid = false;
        } else {
            if (dim.getLarguraCm() == null || dim.getLarguraCm().compareTo(BigDecimal.ZERO) <= 0) {
                String message = "A largura deve ser um número positivo. Valor recebido: " + dim.getLarguraCm();
                context.buildConstraintViolationWithTemplate(message)
                        .addPropertyNode("dimensoesUnitarias.largura").addConstraintViolation();
                valid = false;
            }
            if (dim.getComprimentoCm() == null || dim.getComprimentoCm().compareTo(BigDecimal.ZERO) <= 0) {
                String message = "O comprimento deve ser um número positivo. Valor recebido: " + dim.getComprimentoCm();
                context.buildConstraintViolationWithTemplate(message)
                        .addPropertyNode("dimensoesUnitarias.comprimento").addConstraintViolation();
                valid = false;
            }
        }

        return valid;
    }
}