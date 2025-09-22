package com.dcriar.api.validation.validator;

import com.dcriar.api.dto.request.stock.MovimentacaoRequestDTO;
import com.dcriar.api.validation.annotation.ValidMovimentacaoRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class MovimentacaoRequestValidator implements ConstraintValidator<ValidMovimentacaoRequest, MovimentacaoRequestDTO> {

    @Override
    public boolean isValid(MovimentacaoRequestDTO dto, ConstraintValidatorContext context) {
        if (dto == null) return true;

        boolean valid = true;
        context.disableDefaultConstraintViolation();

        if (dto.getTipo() == null) {
            context.buildConstraintViolationWithTemplate("O tipo da movimentação é obrigatório.")
                    .addPropertyNode("tipo").addConstraintViolation();
            valid = false;
        }

        if (dto.getQuantidade() == null) {
            context.buildConstraintViolationWithTemplate("A quantidade é obrigatória.")
                    .addPropertyNode("quantidade").addConstraintViolation();
            valid = false;
        } else {
            if ((dto.getTipo() != null) &&
                    (dto.getTipo().name().startsWith("SAIDA") || dto.getTipo().name().startsWith("PERDA")) &&
                    dto.getQuantidade().compareTo(BigDecimal.ZERO) > 0) {
                context.buildConstraintViolationWithTemplate("Para saídas ou perdas, a quantidade deve ser negativa.")
                        .addPropertyNode("quantidade").addConstraintViolation();
                valid = false;
            }
        }

        return valid;
    }
}
