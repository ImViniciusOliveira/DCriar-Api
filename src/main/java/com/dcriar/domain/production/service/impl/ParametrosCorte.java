package com.dcriar.domain.production.service.impl;

import lombok.*;

import java.math.BigDecimal;

/**
 * Classe auxiliar para agrupar os parâmetros de corte extraídos e calculados.
 * <p>
 * Utilizada internamente no serviço de produção para organizar os dados
 * necessários para o cálculo de otimização de corte.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ParametrosCorte {

    /**
     * A largura total do lote de matéria-prima em centímetros.
     */
    private BigDecimal larguraTotalLoteCm;

    /**
     * A largura do produto final em centímetros.
     */
    private BigDecimal larguraProduto;

    /**
     * O comprimento do produto final em centímetros.
     */
    private BigDecimal comprimentoProduto;

    /**
     * A quantidade de produtos a serem produzidos.
     */
    private int quantidade;

    /**
     * A margem esquerda a ser aplicada ao corte, em centímetros.
     */
    private BigDecimal margemEsquerda;

    /**
     * A margem direita a ser aplicada ao corte, em centímetros.
     */
    private BigDecimal margemDireita;

    /**
     * A largura útil disponível para corte após a aplicação das margens.
     */
    private BigDecimal larguraUtilCm;

    /**
     * O número de produtos que cabem em uma linha de corte.
     */
    private int produtosPorLinha;

    /**
     * O número de linhas de corte necessárias.
     */
    private int linhas;
}
