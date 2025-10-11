package com.dcriar.api.validation.validator;

import com.dcriar.api.dto.request.production.OrdemDeCorteRequestDTO;
import com.dcriar.api.validation.annotation.ValidOrdemDeCorteRequest;
import com.dcriar.domain.production.enums.ModoCalculo;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador para a requisição de criação de Ordem de Corte.
 * Garante que, se o modo de cálculo for MANUAL, as dimensões finais sejam fornecidas.
 */
public class OrdemDeCorteRequestValidator implements ConstraintValidator<ValidOrdemDeCorteRequest, OrdemDeCorteRequestDTO> {

    @Override
    public boolean isValid(OrdemDeCorteRequestDTO dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true; // A validação de nulidade é feita pelo @NotNull no controller
        }

        // Se o modo de cálculo for MANUAL, as dimensões finais são obrigatórias.
        if (dto.getModoCalculo() == ModoCalculo.MANUAL) {
            if (dto.getLarguraFinalCm() == null || dto.getComprimentoFinalCm() == null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Para o modo de cálculo MANUAL, os campos 'larguraFinalCm' e 'comprimentoFinalCm' são obrigatórios.")
                        .addPropertyNode("larguraFinalCm").addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}
