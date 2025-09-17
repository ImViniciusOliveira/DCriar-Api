package com.dcriar.domain.product.entity;

import com.dcriar.domain.stock.entity.TipoMateriaPrima;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entidade que representa a "receita" de um produto, ligando um Produto a um Tipo de Matéria-Prima.
 * <p>
 * Esta classe funciona como uma tabela de associação com um campo extra,
 * armazenando a quantidade de material gasta para produzir uma unidade do produto.
 */
@Entity
@Table(name = "composicao_produto")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = {"produto", "tipoMateriaPrima"}) // A unicidade é por produto e tipo de material.
public class ComposicaoProduto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    /**
     * CORREÇÃO: A composição agora se liga a um TipoMateriaPrima, não a uma MateriaPrima genérica.
     * A anotação @JoinColumn especifica que a chave estrangeira no banco é 'tipo_materia_prima_id',
     * alinhando o código Java com o script V1 do Flyway.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_materia_prima_id", nullable = false)
    private TipoMateriaPrima tipoMateriaPrima;

    /**
     * O nome do campo agora corresponde à coluna genérica no banco de dados.
     */
    @Column(name = "gasto_material_por_unidade", nullable = false)
    private BigDecimal gastoMaterialPorUnidade;
}

