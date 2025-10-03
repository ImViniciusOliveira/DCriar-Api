package com.dcriar.domain.product.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Representa o estoque de um produto acabado em um canal de venda específico.
 * <p>
 * Esta entidade liga um Produto a um Canal de Venda e armazena a quantidade
 * disponível nesse canal.
 */
@Entity
@Table(name = "estoques")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = "id")
public class Estoque {

    /**
     * O ID único do registro de estoque.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * O produto associado a este registro de estoque.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    /**
     * O canal de venda onde este estoque está alocado.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canal_venda_id", nullable = false)
    private CanalVenda canalVenda;

    /**
     * A quantidade de unidades do produto disponíveis neste canal de venda.
     */
    @Column(nullable = false)
    private Integer quantidade;
}
