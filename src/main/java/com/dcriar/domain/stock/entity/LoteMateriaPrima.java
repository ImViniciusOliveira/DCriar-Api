package com.dcriar.domain.stock.entity;

import com.dcriar.domain.stock.entity.enuns.UnidadeDeMedida;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Representa um lote físico e rastreável de uma matéria-prima no estoque.
 * <p>
 * Cada instância corresponde a um item tangível, como um rolo de adesivo específico
 * ou um galão de tinta, com seus próprios atributos e histórico de movimentações.
 */
@Entity
@Table(name = "lotes_materia_prima")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString(exclude = {"movimentacoes", "loteDeOrigem"})
@EqualsAndHashCode(of = "id")
public class LoteMateriaPrima {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_materia_prima_id", nullable = false)
    private TipoMateriaPrima tipoMateriaPrima;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidade_de_estoque", nullable = false, length = 30)
    private UnidadeDeMedida unidadeDeEstoque;

    /**
     * Campo JSONB para armazenar atributos flexíveis do lote, como 'larguraMm' para rolos.
     */
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> atributos;

    /**
     * O histórico de movimentações deste lote.
     * Usamos LAZY fetch para otimizar a performance, garantindo que o histórico
     * só seja carregado do banco de dados quando for explicitamente necessário.
     */
    @OneToMany(mappedBy = "lote", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MovimentacaoEstoqueLote> movimentacoes = new ArrayList<>();

    /**
     * Relação opcional que liga um lote de sobra (retalho) ao seu lote de origem.
     * Essencial para a rastreabilidade da produção.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_de_origem_id")
    private LoteMateriaPrima loteDeOrigem;

    /**
     * Campo transiente para expor o saldo calculado.
     * Este valor é calculado sob demanda pelo serviço a partir das movimentações.
     * A anotação @Transient impede que o JPA tente criar uma coluna para ele no banco.
     */
    @Transient
    private BigDecimal saldoCalculado;
}

