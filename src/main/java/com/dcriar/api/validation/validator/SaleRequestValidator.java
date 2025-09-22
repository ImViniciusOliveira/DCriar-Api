package com.dcriar.api.validation.validator;

import com.dcriar.api.dto.request.sales.SaleRequestDTO;
import com.dcriar.api.dto.request.sales.SaleItemRequestDTO;
import com.dcriar.api.validation.annotation.ValidSaleRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class SaleRequestValidator implements ConstraintValidator<ValidSaleRequest, SaleRequestDTO> {

    @Override
    public boolean isValid(SaleRequestDTO dto, ConstraintValidatorContext context) {
        if (dto == null) return true;

        boolean valid = true;
        context.disableDefaultConstraintViolation();

        if (dto.getCanalVendaId() == null) {
            context.buildConstraintViolationWithTemplate("O ID do canal de venda é obrigatório.")
                    .addPropertyNode("canalVendaId").addConstraintViolation();
            valid = false;
        }

        List<SaleItemRequestDTO> items = dto.getItems();
        if (items == null || items.isEmpty()) {
            context.buildConstraintViolationWithTemplate("A lista de itens não pode estar vazia.")
                    .addPropertyNode("items").addConstraintViolation();
            valid = false;
        }

        return valid;
    }
}
