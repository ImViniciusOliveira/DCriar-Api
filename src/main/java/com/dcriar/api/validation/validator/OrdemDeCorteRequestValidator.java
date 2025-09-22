package com.dcriar.api.validation.validator;

import com.dcriar.api.dto.request.production.OrdemDeCorteRequestDTO;
import com.dcriar.api.validation.annotation.ValidOrdemDeCorteRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class OrdemDeCorteRequestValidator implements ConstraintValidator<ValidOrdemDeCorteRequest, OrdemDeCorteRequestDTO> {

    @Override
    public boolean isValid(OrdemDeCorteRequestDTO dto, ConstraintValidatorContext context) {
        if (dto == null) return true;

        boolean valid = true;
        context.disableDefaultConstraintViolation();

        if (dto.getLotePrincipalId() == null) {
            context.buildConstraintViolationWithTemplate("O ID do lote principal é obrigatório.")
                    .addPropertyNode("lotePrincipalId").addConstraintViolation();
            valid = false;
        }

        if (dto.getComprimentoDeCorteCm() == null || dto.getComprimentoDeCorteCm().compareTo(BigDecimal.ZERO) <= 0) {
            context.buildConstraintViolationWithTemplate("O comprimento de corte deve ser positivo.")
                    .addPropertyNode("comprimentoDeCorteCm").addConstraintViolation();
            valid = false;
        }

        if (dto.getLarguraDeCorteCm() == null || dto.getLarguraDeCorteCm().compareTo(BigDecimal.ZERO) <= 0) {
            context.buildConstraintViolationWithTemplate("A largura de corte deve ser positiva.")
                    .addPropertyNode("larguraDeCorteCm").addConstraintViolation();
            valid = false;
        }

        if (dto.getProdutoId() == null) {
            context.buildConstraintViolationWithTemplate("O ID do produto final é obrigatório.")
                    .addPropertyNode("produtoId").addConstraintViolation();
            valid = false;
        }

        if (dto.getQuantidadeProduzida() == null || dto.getQuantidadeProduzida() <= 0) {
            context.buildConstraintViolationWithTemplate("A quantidade produzida deve ser positiva.")
                    .addPropertyNode("quantidadeProduzida").addConstraintViolation();
            valid = false;
        }

        if (dto.getCanalVendaDestinoId() == null) {
            context.buildConstraintViolationWithTemplate("O ID do canal de venda de destino é obrigatório.")
                    .addPropertyNode("canalVendaDestinoId").addConstraintViolation();
            valid = false;
        }

        return valid;
    }
}
