package com.dcriar.domain.production.service.impl;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Encapsula os parâmetros de corte para o processamento de ordens de produção.
 */
@Getter
@Builder
public class ParametrosCorte {

    /**
     * A largura total do lote em centímetros.
     */
    private final BigDecimal larguraTotalLoteCm;

    /**
     * A largura do produto em centímetros.
     */
    private final BigDecimal larguraProduto;

    /**
     * O comprimento do produto em centímetros.
     */
    private final BigDecimal comprimentoProduto;

    /**
     * A quantidade de produtos a serem produzidos.
     */
    private final int quantidade;

    /**
     * A margem esquerda em centímetros.
     */
    private final BigDecimal margemEsquerda;

    /**
     * A margem direita em centímetros.
     */
    private final BigDecimal margemDireita;

    /**
     * A largura útil do lote em centímetros, desconsiderando as margens.
     */
    private final BigDecimal larguraUtilCm;

    /**
     * O número de produtos por linha.
     */
    private final int produtosPorLinha;

    /**
     * O número de linhas.
     */
    private final int linhas;
}
