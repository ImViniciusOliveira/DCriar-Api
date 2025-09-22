package com.dcriar.exception.custom;

import lombok.Getter;

@Getter
public class EstoqueInsuficienteException extends RuntimeException {
    private final Long produtoId;
    private Long canalVendaId;
    private Integer quantidadeRequisitada;

    /**
     * Construtor para erros específicos de falta de estoque num canal de venda.
     */
    public EstoqueInsuficienteException(Long produtoId, Long canalVendaId, int quantidadeRequisitada) {
        super(String.format(
                "Estoque insuficiente para o produto ID %d no canal de venda ID %d. Tentativa de remover %d unidades.",
                produtoId, canalVendaId, quantidadeRequisitada
        ));
        this.produtoId = produtoId;
        this.canalVendaId = canalVendaId;
        this.quantidadeRequisitada = quantidadeRequisitada;
    }

    /**
     * Construtor para erros genéricos de violação de regras de estoque, como a do Estoque Mestre.
     */
    public EstoqueInsuficienteException(Long produtoId, String message) {
        super(message);
        this.produtoId = produtoId;
    }
}

