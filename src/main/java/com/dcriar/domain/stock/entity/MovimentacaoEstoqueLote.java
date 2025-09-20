package com.dcriar.domain.stock.entity;

import com.dcriar.domain.stock.entity.enuns.TipoMovimentacao;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Entidade que representa um único registro no "Livro-Razão" do estoque.
 */
@Entity
@Table(name = "movimentacoes_estoque_lote")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = "id")
public class MovimentacaoEstoqueLote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    private LoteMateriaPrima lote;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime data;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoMovimentacao tipo;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal quantidade;

    /**
     * O custo calculado por unidade de consumo (ex: R$/cm², R$/ml).
     * Este campo só é preenchido em movimentações de ENTRADA_COMPRA.
     */
    @Column(name = "custo_por_unidade_base", precision = 19, scale = 8)
    private BigDecimal custoPorUnidadeBase;

    @Column(length = 254)
    private String motivo;

}

