package com.dcriar.api.validation.validator;

import com.dcriar.api.dto.request.production.OrdemDeCorteRequestDTO;
import com.dcriar.api.validation.annotation.ValidOrdemDeCorteRequest;
import com.dcriar.domain.production.enums.ModoCalculo;

/**
 * Validador para o DTO {@link OrdemDeCorteRequestDTO}, acionado pela anotação {@link ValidOrdemDeCorteRequest}.
 * <p>
 * Este validador verifica as regras de negócio para uma ordem de corte:
 * <ul>
 *     <li>Campos básicos como {@code lotePrincipalId}, {@code produtoId}, {@code quantidadeProduzida},
 *     {@code canalVendaDestinoId} e {@code modoCalculo} são obrigatórios.</li>
 *     <li>A {@code quantidadeProduzida} deve ser um número positivo.</li>
 *     <li>Se o {@code modoCalculo} for AUTOMATICO, o campo {@code margens} é obrigatório.</li>
 *     <li>Se o {@code modoCalculo} for MANUAL, o campo {@code tamanhoFinal} é obrigatório.</li>
 * </ul>
 */
public class OrdemDeCorteRequestValidator extends BaseValidator<ValidOrdemDeCorteRequest, OrdemDeCorteRequestDTO> {

    @Override
    protected void validate(OrdemDeCorteRequestDTO dto) {
        addViolationIf(dto.getLotePrincipalId() == null, "O ID do lote principal é obrigatório.", "lotePrincipalId");
        addViolationIf(dto.getProdutoId() == null, "O ID do produto é obrigatório.", "produtoId");
        addViolationIf(dto.getCanalVendaDestinoId() == null, "O ID do canal de venda de destino é obrigatório.", "canalVendaDestinoId");
        addViolationIf(dto.getQuantidadeProduzida() == null || dto.getQuantidadeProduzida() <= 0, "A quantidade produzida deve ser um número positivo.", "quantidadeProduzida");

        ModoCalculo modoCalculo = dto.getModoCalculo();
        addViolationIf(modoCalculo == null, "O modo de cálculo é obrigatório.", "modoCalculo");

        if (modoCalculo != null) {
            addViolationIf(modoCalculo == ModoCalculo.AUTOMATICO && dto.getMargens() == null, "O campo 'margens' é obrigatório para o modo de cálculo AUTOMATICO.", "margens");
            addViolationIf(modoCalculo == ModoCalculo.MANUAL && dto.getTamanhoFinal() == null, "O campo 'tamanhoFinal' é obrigatório para o modo de cálculo MANUAL.", "tamanhoFinal");
        }
    }
}
