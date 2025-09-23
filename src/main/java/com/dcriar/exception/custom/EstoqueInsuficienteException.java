package com.dcriar.exception.custom;

import lombok.Getter;

/**
 * Exceção de negócio lançada para qualquer violação de regras de estoque.
 * <p>
 * Esta classe foi refatorada para ser o contentor central para todos os erros de
 * estoque, carregando o máximo de contexto possível para gerar mensagens
 * detalhadas e úteis para o utilizador.
 */
@Getter
public class EstoqueInsuficienteException extends RuntimeException {

    private final Long produtoId;
    private final Long canalVendaId;
    private final Integer quantidadeRequisitada;
    private final Integer estoqueAtual;

    /**
     * Construtor principal para erros de falta de estoque num canal de venda.
     * Gera a mensagem de erro mais detalhada possível.
     *
     * @param produtoId O ID do produto.
     * @param canalVendaId O ID do canal de venda.
     * @param quantidadeRequisitada A quantidade que se tentou remover.
     * @param estoqueAtual A quantidade que estava disponível no momento da falha.
     */
    public EstoqueInsuficienteException(Long produtoId, Long canalVendaId, int quantidadeRequisitada, int estoqueAtual) {
        super(String.format(
                "Estoque insuficiente no canal. Tentativa de remover %d unidades do produto ID %d no canal ID %d, mas apenas %d unidades estavam disponíveis.",
                Math.abs(quantidadeRequisitada), produtoId, canalVendaId, estoqueAtual
        ));
        this.produtoId = produtoId;
        this.canalVendaId = canalVendaId;
        this.quantidadeRequisitada = quantidadeRequisitada;
        this.estoqueAtual = estoqueAtual;
    }

    /**
     * Construtor para erros genéricos de violação de regras de estoque, como a do Estoque Mestre,
     * onde o contexto do canal ou da quantidade pode não ser aplicável.
     */
    public EstoqueInsuficienteException(Long produtoId, String message) {
        super(message);
        this.produtoId = produtoId;
        this.canalVendaId = null;
        this.quantidadeRequisitada = null;
        this.estoqueAtual = null;
    }
}

