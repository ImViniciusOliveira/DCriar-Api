package com.dcriar.api.validation.validator;

import com.dcriar.api.dto.request.stock.LoteMateriaPrimaRequestDTO;
import com.dcriar.api.validation.annotation.ValidLoteMateriaPrimaRequest;
import com.dcriar.domain.stock.entity.enums.UnidadeDeMedida;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;
import java.util.Map;

public class LoteMateriaPrimaRequestValidator implements ConstraintValidator<ValidLoteMateriaPrimaRequest, LoteMateriaPrimaRequestDTO> {

    @Override
    public boolean isValid(LoteMateriaPrimaRequestDTO dto, ConstraintValidatorContext context) {
        if (dto == null) return true;

        boolean valid = true;
        context.disableDefaultConstraintViolation();

        if (dto.getTipoMateriaPrimaId() == null) {
            context.buildConstraintViolationWithTemplate("O ID do tipo de matéria-prima é obrigatório.")
                    .addPropertyNode("tipoMateriaPrimaId").addConstraintViolation();
            valid = false;
        }

        if (dto.getUnidadeDeEstoque() == null) {
            context.buildConstraintViolationWithTemplate("A unidade de estoque é obrigatória.")
                    .addPropertyNode("unidadeDeEstoque").addConstraintViolation();
            valid = false;
        }

        if (dto.getQuantidadeInicial() == null || dto.getQuantidadeInicial().compareTo(BigDecimal.ZERO) <= 0) {
            context.buildConstraintViolationWithTemplate("A quantidade inicial deve ser maior que zero.")
                    .addPropertyNode("quantidadeInicial").addConstraintViolation();
            valid = false;
        }

        if (dto.getCustoTotalLote() == null || dto.getCustoTotalLote().compareTo(BigDecimal.ZERO) <= 0) {
            context.buildConstraintViolationWithTemplate("O custo total do lote deve ser maior que zero.")
                    .addPropertyNode("custoTotalLote").addConstraintViolation();
            valid = false;
        }

        if (dto.getUnidadeDeEstoque() == UnidadeDeMedida.METRO_LINEAR) {
            Map<String, Object> atributos = dto.getAtributos();
            if (atributos == null || !atributos.containsKey("larguraMm")) {
                context.buildConstraintViolationWithTemplate("Para unidade METRO_LINEAR, o atributo 'larguraMm' é obrigatório.")
                        .addPropertyNode("atributos").addConstraintViolation();
                valid = false;
            }
        }

        return valid;
    }
}
