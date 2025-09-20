package com.dcriar.domain.sales.entity;

import com.dcriar.domain.product.entity.CanalVenda;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa o "cabeçalho" de uma venda (Sale) realizada.
 * <p>
 * Esta entidade agrupa todos os itens de uma única transação e armazena
 * informações gerais como a data, o canal de venda e o valor total.
 */
@Entity
@Table(name = "sales")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = "id")
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "sale_date", nullable = false, updatable = false)
    private OffsetDateTime saleDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canal_venda_id", nullable = false)
    private CanalVenda canalVenda;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    /**
     * A lista de itens que compõem esta venda.
     * O mappedBy agora aponta para o campo 'sale' na entidade SaleItem.
     */
    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SaleItem> items = new ArrayList<>();

    /**
     * Método auxiliar para adicionar um item à venda, garantindo a consistência
     * da relação bidirecional.
     *
     * @param item O SaleItem a ser adicionado.
     */
    public void addItem(SaleItem item) {
        this.items.add(item);
        item.setSale(this);
    }
}

