package com.dcriar.domain.product.entity;

import com.dcriar.api.dto.request.product.ProdutoRequestDTO;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Representa um produto final (etiqueta) no sistema da DCriar.
 * <p>
 * Esta entidade armazena as características principais de um produto e sua "receita".
 * Adota o padrão "Rich Domain Model", onde a própria entidade contém a lógica de
 * negócio para sua criação e atualização.
 */
@Entity
@Table(name = "produtos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@ToString(exclude = "composicao")
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

    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<ComposicaoProduto> composicao = new HashSet<>();

    /**
     * Método de fábrica estático para criar um novo Produto a partir de um DTO.
     * <p>
     * Encapsula a lógica de criação e as regras de negócio.
     *
     * @param request O DTO com os dados para a criação.
     * @return Uma nova entidade {@link Produto}, pronta para ser persistida.
     */
    public static Produto from(ProdutoRequestDTO request) {
        return Produto.builder()
                .nome(request.getNome())
                .sku(request.getSku())
                .descricao(request.getDescricao())
                .cor(request.getCor())
                .unidadesPorProduto(request.getUnidadesPorProduto())
                .fotoPrincipalUrl(request.getFotoPrincipalUrl())
                .ativo(false)
                .build();
    }

    /**
     * Atualiza os dados do produto a partir de um DTO.
     *
     * @param request O DTO com os dados para a atualização.
     */
    public void updateFrom(ProdutoRequestDTO request) {
        this.nome = request.getNome();
        this.sku = request.getSku();
        this.descricao = request.getDescricao();
        this.cor = request.getCor();
        this.unidadesPorProduto = request.getUnidadesPorProduto();
        this.fotoPrincipalUrl = request.getFotoPrincipalUrl();
        this.ativo = request.getAtivo() != null ? request.getAtivo() : this.ativo;
    }

    /**
     * Adiciona um item à "receita" do produto, garantindo a consistência da relação bidirecional.
     *
     * @param item A entidade {@link ComposicaoProduto} a ser adicionada.
     */
    public void adicionarComposicao(ComposicaoProduto item) {
        this.composicao.add(item);
        item.setProduto(this);
    }

    /**
     * Limpa a composição atual do produto. Essencial para a lógica de atualização.
     */
    public void limparComposicao() {
        this.composicao.clear();
    }
}

