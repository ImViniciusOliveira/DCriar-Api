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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canal_venda_id", nullable = false)
    private CanalVenda canalVenda;

    @Column(nullable = false)
    private Integer quantidade;
}
