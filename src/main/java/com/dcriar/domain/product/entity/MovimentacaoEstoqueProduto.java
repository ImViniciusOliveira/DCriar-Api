package com.dcriar.domain.product.entity;

import com.dcriar.domain.product.entity.enums.TipoMovimentacaoProduto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * Entidade que representa um único registo no "Livro-Razão" do estoque de produtos acabados.
 * <p>
 * Cada instância desta classe é um evento imutável que descreve uma mudança
 * na quantidade de um Produto específico. A soma de todas as
 * movimentações de um produto resulta no seu saldo físico total.
 */
@Entity
@Table(name = "movimentacoes_estoque_produto")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = "id")
public class MovimentacaoEstoqueProduto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime data;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoMovimentacaoProduto tipo;

    /**
     * Quantidade movimentada. Positiva para entradas, negativa para saídas.
     */
    @Column(nullable = false)
    private Integer quantidade;

    @Column(length = 254)
    private String motivo;
}
