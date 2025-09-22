package com.dcriar.api.validation.validator;

import com.dcriar.api.dto.request.product.ComposicaoRequestDTO;

import com.dcriar.api.validation.annotation.ValidComposicaoRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

public class ComposicaoRequestValidator implements ConstraintValidator<ValidComposicaoRequest, ComposicaoRequestDTO> {

    @Override
    public boolean isValid(ComposicaoRequestDTO composicao, ConstraintValidatorContext context) {
        if (composicao == null) {
            return true; // null será tratado por ProdutoRequestDTO se necessário
        }

        List<String> erros = new ArrayList<>();

        if (composicao.getMateriaPrimaId() == null) {
            erros.add("materiaPrimaId é obrigatório");
        }

        if (composicao.getGastoMaterialPorUnidade() == null) {
            erros.add("gastoMaterialPorUnidade é obrigatório");
        }

        if (!erros.isEmpty()) {
            context.disableDefaultConstraintViolation();
            erros.forEach(erro -> context.buildConstraintViolationWithTemplate(erro)
                    .addConstraintViolation());
            return false;
        }

        return true;
    }
}
