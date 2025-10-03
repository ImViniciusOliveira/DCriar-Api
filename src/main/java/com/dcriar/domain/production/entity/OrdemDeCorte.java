package com.dcriar.domain.production.entity;

import com.dcriar.domain.product.entity.Produto;
import com.dcriar.domain.production.enums.ModoCalculo;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa uma Ordem de Corte no sistema, detalhando a produção de um produto a partir de matéria-prima.
 * <p>
 * Esta entidade registra qual produto foi produzido, a partir de qual lote de matéria-prima,
 * a quantidade produzida, o modo de cálculo utilizado e as dimensões finais do corte.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@ToString(exclude = "cortesRealizados")
@EqualsAndHashCode(of = "id")
@Table(name = "ordens_de_corte")
public class OrdemDeCorte {

    /**
     * O ID único da ordem de corte.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * O produto final que está sendo fabricado por esta ordem de corte.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    /**
     * O ID do lote de matéria-prima principal de onde o material foi consumido.
     */
    @Column(name = "lote_principal_id", nullable = false)
    private Long lotePrincipalId;

    /**
     * O ID do canal de venda para o qual o estoque será destinado.
     */
    @Column(name = "canal_venda_destino_id")
    private Long canalVendaDestinoId;

    /**
     * A quantidade de unidades do produto final que foram produzidas com sucesso.
     */
    @Column(name = "quantidade_produzida", nullable = false)
    private Integer quantidadeProduzida;

    /**
     * O modo de cálculo utilizado para esta ordem de corte (AUTOMATICO ou MANUAL).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "modo_calculo", nullable = false, length = 20)
    private ModoCalculo modoCalculo;

    @Embedded
    private Margens margens;

    @OneToMany(mappedBy = "ordemDeCorte", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<CorteRealizado> cortesRealizados = new ArrayList<>();

    /**
     * A largura final do corte em centímetros.
     */
    @Column(name = "largura_final_cm", nullable = false, precision = 10, scale = 2)
    private BigDecimal larguraFinalCm;

    /**
     * O comprimento final do corte em centímetros.
     */
    @Column(name = "comprimento_final_cm", nullable = false, precision = 10, scale = 2)
    private BigDecimal comprimentoFinalCm;

    /**
     * A data e hora em que a ordem de corte foi criada. Gerado automaticamente no momento da criação.
     */
    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private OffsetDateTime dataCriacao;

    /**
     * Um motivo, observação ou referência para a ordem de produção (ex: número do pedido do cliente).
     */
    @Column(name = "motivo")
    private String motivo;

    public void addCorteRealizado(CorteRealizado corte) {
        cortesRealizados.add(corte);
        corte.setOrdemDeCorte(this);
    }
}
