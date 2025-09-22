package com.dcriar.api.validation.validator;

import com.dcriar.api.dto.request.sales.SaleItemRequestDTO;
import com.dcriar.api.validation.annotation.ValidSaleItemRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SaleItemRequestValidator implements ConstraintValidator<ValidSaleItemRequest, SaleItemRequestDTO> {

    @Override
    public boolean isValid(SaleItemRequestDTO dto, ConstraintValidatorContext context) {
        if (dto == null) return true;

        boolean valid = true;
        context.disableDefaultConstraintViolation();

        if (dto.getProdutoId() == null) {
            context.buildConstraintViolationWithTemplate("O ID do produto é obrigatório.")
                    .addPropertyNode("produtoId").addConstraintViolation();
            valid = false;
        }

        if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
            context.buildConstraintViolationWithTemplate("A quantidade deve ser positiva.")
                    .addPropertyNode("quantity").addConstraintViolation();
            valid = false;
        }

        return valid;
    }
}
