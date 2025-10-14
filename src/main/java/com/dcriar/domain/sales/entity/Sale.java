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
@ToString(exclude = {"items"})
@EqualsAndHashCode(of = "id")
public class Sale {

    /**
     * O ID único da venda.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * A data e hora em que a venda foi registrada.
     * Gerado automaticamente no momento da criação.
     */
    @CreationTimestamp
    @Column(name = "sale_date", nullable = false, updatable = false)
    private OffsetDateTime saleDate;

    /**
     * O canal de venda onde esta transação ocorreu.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canal_venda_id", nullable = false)
    private CanalVenda canalVenda;

    /**
     * O valor total da venda, somando os preços de todos os itens.
     */
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    /**
     * A lista de itens que compõem esta venda.
     * A relação é bidirecional, e o {@code mappedBy} aponta para o campo 'sale' na entidade SaleItem.
     * O {@code CascadeType.ALL} garante que operações como persistência e remoção se propaguem para os itens.
     * O {@code orphanRemoval = true} garante que itens removidos da lista sejam deletados do banco de dados.
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

    /**
     * Cria uma instância de Sale a partir dos dados informados, centralizando regras de negócio de criação.
     * <p>
     * Este método deve ser utilizado pela service para garantir consistência e aplicar validações extras.
     * CanalVenda e SaleItem devem ser resolvidos previamente na service.
     *
     * @param canalVenda Canal de venda resolvido
     * @param items Lista de itens da venda já convertidos
     * @return Sale criada
     */
    public static Sale from(CanalVenda canalVenda, List<SaleItem> items) {
        Sale sale = Sale.builder()
                .canalVenda(canalVenda)
                .items(new ArrayList<>())
                .build();
        if (items != null) {
            for (SaleItem item : items) {
                sale.addItem(item);
            }
        }
        // O valor total deve ser calculado e atribuído na service
        return sale;
    }

    /**
     * Atualiza os campos da Sale existente a partir dos dados informados, centralizando regras de negócio de atualização.
     * <p>
     * Este método deve ser utilizado pela service para garantir consistência e aplicar validações extras.
     * CanalVenda e SaleItem devem ser resolvidos previamente na service.
     *
     * @param canalVenda Canal de venda resolvido
     * @param items Lista de itens da venda já convertidos
     */
    public void updateFrom(CanalVenda canalVenda, List<SaleItem> items) {
        this.canalVenda = canalVenda;
        this.items.clear();
        if (items != null) {
            for (SaleItem item : items) {
                this.addItem(item);
            }
        }
    }
}
