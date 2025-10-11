package com.dcriar.domain.production.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Representa um corte específico realizado como parte de uma {@link OrdemDeProducao}.
 * <p>
 * Cada instância registra as dimensões e a quantidade de peças cortadas,
 * especificando se o corte resultou em um produto final ou em um retalho (sobra de material).
 * Este conceito é aplicável apenas a produções do tipo 'Corte'.
 */
@Entity
@Table(name = "cortes_realizados")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
@EqualsAndHashCode(of = "id")
public class CorteRealizado {

    /**
     * O ID único do registro de corte.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * A ordem de produção à qual este corte está associado.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordem_de_producao_id", nullable = false)
    private OrdemDeProducao ordemDeProducao;

    /**
     * A largura do corte em centímetros.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal larguraCm;

    /**
     * O comprimento do corte em centímetros.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal comprimentoCm;

    /**
     * A quantidade de peças idênticas produzidas com este corte.
     */
    @Column(nullable = false)
    private int quantidade;

    /**
     * O tipo de resultado do corte.
     * <p>
     * Pode ser "PRODUTO" para um item final ou "RETALHO" para sobras de material.
     */
    @Column(nullable = false, length = 20)
    private String tipo;
}