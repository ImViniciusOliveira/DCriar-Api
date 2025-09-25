package com.dcriar.domain.product.entity;

import com.dcriar.api.dto.request.product.ProdutoRequestDTO;
import com.dcriar.domain.stock.entity.TipoMateriaPrima;
import jakarta.persistence.*;
import lombok.*;

/**
 * Representa um produto final (o "molde") no sistema da DCriar.
 * <p>
 * Esta entidade foi refatorada para ser o "molde" de um produto. Ela armazena
 * as dimensões de uma única unidade e o tipo de matéria-prima principal que utiliza,
 * removendo a antiga lógica de composição para simplificar o modelo.
 */
@Entity
@Table(name = "produtos")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@ToString
@EqualsAndHashCode(of = "id")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false, length = 50)
    private String cor;

    @Column(nullable = false)
    private Integer unidadesPorProduto;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "foto_principal_url")
    private String fotoPrincipalUrl;

    /**
     * Ligação direta ao tipo de matéria-prima que este produto consome.
     * Simplifica o modelo, assumindo que cada produto é feito de um material principal.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_materia_prima_id")
    private TipoMateriaPrima tipoMateriaPrima;

    /**
     * As dimensões de uma única unidade deste produto (o "molde").
     */
    @Embedded
    private Dimensoes dimensoesUnitarias;

    /**
     * Método de fábrica estático para criar um novo Produto a partir de um DTO.
     * A responsabilidade de buscar e definir o TipoMateriaPrima é do serviço.
     */
    public static Produto from(ProdutoRequestDTO request) {
        Dimensoes dimensoes = request.getDimensoesUnitarias() != null
                ? new Dimensoes(request.getDimensoesUnitarias().getLarguraCm(), request.getDimensoesUnitarias().getComprimentoCm())
                : null;

        return Produto.builder()
                .nome(request.getNome())
                .sku(request.getSku())
                .descricao(request.getDescricao())
                .cor(request.getCor())
                .unidadesPorProduto(request.getUnidadesPorProduto())
                .fotoPrincipalUrl(request.getFotoPrincipalUrl())
                .ativo(request.getAtivo() != null ? request.getAtivo() : false)
                .dimensoesUnitarias(dimensoes)
                .build();
    }

    /**
     * Atualiza os dados do produto a partir de um DTO.
     */
    public void updateFrom(ProdutoRequestDTO request) {
        this.nome = request.getNome();
        this.sku = request.getSku();
        this.descricao = request.getDescricao();
        this.cor = request.getCor();
        this.unidadesPorProduto = request.getUnidadesPorProduto();
        this.fotoPrincipalUrl = request.getFotoPrincipalUrl();
        this.ativo = request.getAtivo() != null ? request.getAtivo() : this.ativo;

        if (request.getDimensoesUnitarias() != null) {
            this.dimensoesUnitarias = new Dimensoes(request.getDimensoesUnitarias().getLarguraCm(), request.getDimensoesUnitarias().getComprimentoCm());
        }
    }
}
