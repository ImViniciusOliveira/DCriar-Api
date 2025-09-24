package com.dcriar.api.validation.validator;

import com.dcriar.api.dto.request.production.MargensRequestDTO;
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

        // Validações de tamanho unitário (modo MANUAL)
        if (dto.getModoCalculo() != null && dto.getModoCalculo().name().equals("MANUAL")) {
            if (dto.getTamanhoFinal() == null || dto.getTamanhoFinal().getComprimento() == null || dto.getTamanhoFinal().getComprimento().compareTo(BigDecimal.ZERO) <= 0) {
                context.buildConstraintViolationWithTemplate("O comprimento final do produto deve ser positivo.")
                        .addPropertyNode("tamanhoFinal.comprimento").addConstraintViolation();
                valid = false;
            }
            if (dto.getTamanhoFinal() == null || dto.getTamanhoFinal().getLargura() == null || dto.getTamanhoFinal().getLargura().compareTo(BigDecimal.ZERO) <= 0) {
                context.buildConstraintViolationWithTemplate("A largura final do produto deve ser positiva.")
                        .addPropertyNode("tamanhoFinal.largura").addConstraintViolation();
                valid = false;
            }
        }

        // Validações de margens (modo AUTOMATICO)
        if (dto.getModoCalculo() != null && dto.getModoCalculo().name().equals("AUTOMATICO")) {
            MargensRequestDTO margens = dto.getMargens();
            if (margens != null) {
                if (margens.getSuperior() != null && margens.getSuperior().compareTo(BigDecimal.ZERO) < 0) {
                    context.buildConstraintViolationWithTemplate("A margem superior não pode ser negativa.")
                            .addPropertyNode("margens.superior").addConstraintViolation();
                    valid = false;
                }
                if (margens.getInferior() != null && margens.getInferior().compareTo(BigDecimal.ZERO) < 0) {
                    context.buildConstraintViolationWithTemplate("A margem inferior não pode ser negativa.")
                            .addPropertyNode("margens.inferior").addConstraintViolation();
                    valid = false;
                }
                if (margens.getEsquerda() != null && margens.getEsquerda().compareTo(BigDecimal.ZERO) < 0) {
                    context.buildConstraintViolationWithTemplate("A margem esquerda não pode ser negativa.")
                            .addPropertyNode("margens.esquerda").addConstraintViolation();
                    valid = false;
                }
                if (margens.getDireita() != null && margens.getDireita().compareTo(BigDecimal.ZERO) < 0) {
                    context.buildConstraintViolationWithTemplate("A margem direita não pode ser negativa.")
                            .addPropertyNode("margens.direita").addConstraintViolation();
                    valid = false;
                }
            }
            // Comprimento e largura finais (com margens)
            if (dto.getTamanhoFinal() != null) {
                BigDecimal comprimento = dto.getTamanhoFinal().getComprimento();
                BigDecimal largura = dto.getTamanhoFinal().getLargura();
                BigDecimal margemSuperior = margens != null && margens.getSuperior() != null ? margens.getSuperior() : BigDecimal.ZERO;
                BigDecimal margemInferior = margens != null && margens.getInferior() != null ? margens.getInferior() : BigDecimal.ZERO;
                BigDecimal margemEsquerda = margens != null && margens.getEsquerda() != null ? margens.getEsquerda() : BigDecimal.ZERO;
                BigDecimal margemDireita = margens != null && margens.getDireita() != null ? margens.getDireita() : BigDecimal.ZERO;
                if (comprimento != null && comprimento.add(margemSuperior).add(margemInferior).compareTo(BigDecimal.ZERO) <= 0) {
                    context.buildConstraintViolationWithTemplate("O comprimento total do corte (produto + margens) deve ser positivo.")
                            .addPropertyNode("tamanhoFinal.comprimento").addConstraintViolation();
                    valid = false;
                }
                if (largura != null && largura.add(margemEsquerda).add(margemDireita).compareTo(BigDecimal.ZERO) <= 0) {
                    context.buildConstraintViolationWithTemplate("A largura total do corte (produto + margens) deve ser positiva.")
                            .addPropertyNode("tamanhoFinal.largura").addConstraintViolation();
                    valid = false;
                }
            }
        }
        return valid;
    }
}
