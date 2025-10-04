package com.dcriar.domain.stock.entity;

import com.dcriar.domain.stock.entity.enums.TipoMovimentacao;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Entidade que representa um único registro no "Livro-Razão" do estoque de um lote de matéria-prima.
 * <p>
 * Cada movimentação detalha a data, o tipo (entrada/saída), a quantidade e o motivo,
 * permitindo rastrear o histórico completo de um lote.
 */
@Entity
@Table(name = "movimentacoes_estoque_lote")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
@EqualsAndHashCode(of = "id")
public class MovimentacaoEstoqueLote {

    /**
     * O ID único da movimentação de estoque do lote.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * O lote de matéria-prima ao qual esta movimentação pertence.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    private LoteMateriaPrima lote;

    /**
     * A data e hora em que a movimentação foi registrada.
     * Gerado automaticamente no momento da criação.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime data;

    /**
     * O tipo da movimentação (ex: ENTRADA_COMPRA, SAIDA_PRODUCAO).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoMovimentacao tipo;

    /**
     * A quantidade movimentada. Positiva para entradas, negativa para saídas.
     */
    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal quantidade;

    /**
     * O custo calculado por unidade de consumo (ex: R$/cm², R$/ml).
     * <p>
     * Este campo só é preenchido em movimentações de ENTRADA_COMPRA.
     */
    @Column(name = "custo_por_unidade_base", precision = 19, scale = 8)
    private BigDecimal custoPorUnidadeBase;

    /**
     * O motivo ou observação registrado para a movimentação.
     */
    @Column(length = 254)
    private String motivo;
}
