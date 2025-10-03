package com.dcriar.domain.sales.entity;

import com.dcriar.domain.product.entity.Produto;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Representa um item individual dentro de uma Venda (Sale).
 * <p>
 * Cada instância desta classe é uma "linha do recibo", detalhando qual produto
 * foi vendido, a quantidade e o preço unitário pago no momento da transação.
 */
@Entity
@Table(name = "sale_items")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = "id")
public class SaleItem {

    /**
     * O ID único do item da venda.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * A venda à qual este item pertence.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    /**
     * O produto que foi vendido neste item.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    /**
     * A quantidade de unidades do produto vendidas.
     */
    @Column(nullable = false)
    private Integer quantity;

    /**
     * O preço unitário do produto no momento da venda.
     * É crucial armazenar este valor para garantir a integridade histórica
     * dos dados financeiros, mesmo que o preço do produto mude no futuro.
     */
    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    /**
     * O preço total para este item (quantidade * preço unitário).
     * Armazenado para facilitar consultas e relatórios.
     */
    @Column(name = "total_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPrice;
}
