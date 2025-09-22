package com.dcriar.exception.custom;

import lombok.Getter;

/**
 * Exceção lançada quando uma operação resultaria num saldo de estoque
 * negativo para um produto num canal de venda específico.
 * <p>
 * Esta exceção carrega todo o contexto do erro para permitir a criação
 * de mensagens detalhadas para o utilizador.
 */
@Getter
public class EstoqueNegativoNoCanalException extends RuntimeException {

    private final Long produtoId;
    private final Long canalVendaId;
    private final int estoqueAtual;
    private final int quantidadeRemovida;

    public EstoqueNegativoNoCanalException(Long produtoId, Long canalVendaId, int estoqueAtual, int quantidadeRemovida) {
        super(String.format(
                "Operação resultaria em estoque negativo. Produto ID: %d, Canal ID: %d. Estoque atual: %d, Tentativa de remover: %d.",
                produtoId, canalVendaId, estoqueAtual, Math.abs(quantidadeRemovida)
        ));
        this.produtoId = produtoId;
        this.canalVendaId = canalVendaId;
        this.estoqueAtual = estoqueAtual;
        this.quantidadeRemovida = quantidadeRemovida;
    }
}