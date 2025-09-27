package com.dcriar.domain.production.service.impl;

import java.math.BigDecimal;

/**
 * Classe auxiliar para agrupar os parâmetros de corte extraídos.
 */
public record ParametrosCorte(BigDecimal larguraTotalLoteCm, BigDecimal larguraProduto, BigDecimal comprimentoProduto,
                              int quantidade, BigDecimal margemEsquerda, BigDecimal margemDireita,
                              BigDecimal larguraUtilCm, int produtosPorLinha, int linhas) {
}

