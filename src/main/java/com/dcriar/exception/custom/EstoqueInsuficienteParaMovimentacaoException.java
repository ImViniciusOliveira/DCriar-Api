package com.dcriar.exception.custom;

import lombok.Getter;

/**
 * Exceção de negócio lançada quando uma movimentação de estoque de lote
 * não pode ser registrada devido a saldo insuficiente.
 */
@Getter
public class EstoqueInsuficienteParaMovimentacaoException extends RuntimeException {

    private final Long loteId;
    private final double quantidadeRequisitada;
    private final double saldoDisponivel;

    /**
     * Construtor principal.
     *
     * @param loteId ID do lote no qual a movimentação foi tentada
     * @param quantidadeRequisitada Quantidade que tentou registrar
     * @param saldoDisponivel Saldo disponível no lote
     */
    public EstoqueInsuficienteParaMovimentacaoException(Long loteId, double quantidadeRequisitada, double saldoDisponivel) {
        super(String.format(
                "Tentativa de registrar uma movimentação de %.4f unidades no lote %d, mas o saldo disponível é %.4f",
                Math.abs(quantidadeRequisitada), loteId, saldoDisponivel
        ));
        this.loteId = loteId;
        this.quantidadeRequisitada = quantidadeRequisitada;
        this.saldoDisponivel = saldoDisponivel;
    }
}
