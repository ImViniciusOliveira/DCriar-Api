package com.dcriar.api.hateous.assembler;

import com.dcriar.api.controller.product.EstoqueProdutoController;
import com.dcriar.api.dto.response.product.MovimentacaoProdutoResponseDTO;
import com.dcriar.api.hateous.model.MovimentacaoProdutoModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Assembler responsável por converter {@link MovimentacaoProdutoResponseDTO} em {@link MovimentacaoProdutoModel}
 * e adicionar os links HATEOAS apropriados.
 */
@Component
public class MovimentacaoProdutoModelAssembler extends RepresentationModelAssemblerSupport<MovimentacaoProdutoResponseDTO, MovimentacaoProdutoModel> {

    public MovimentacaoProdutoModelAssembler() {
        super(EstoqueProdutoController.class, MovimentacaoProdutoModel.class);
    }

    /**
     * Converte um {@link MovimentacaoProdutoResponseDTO} em um {@link MovimentacaoProdutoModel} e adiciona links HATEOAS.
     * Para movimentações individuais, o link self pode ser adicionado se houver um endpoint para buscar uma movimentação por ID.
     * No contexto atual, onde as movimentações são listadas por produto, o link self para a movimentação individual não é diretamente aplicável
     * a menos que um endpoint específico para {@code /movimentacoes/{id}} exista.
     *
     * @param dto O DTO de resposta da movimentação.
     * @return O modelo HATEOAS da movimentação com links.
     */
    @Override
    @NonNull
    public MovimentacaoProdutoModel toModel(@NonNull MovimentacaoProdutoResponseDTO dto) {
        MovimentacaoProdutoModel model = MovimentacaoProdutoModel.fromDto(dto);

        // Se houvesse um endpoint para buscar uma movimentação individual por ID, adicionaríamos um link self aqui:
        // model.add(linkTo(methodOn(EstoqueProdutoController.class).getMovimentacaoById(model.getId())).withSelfRel());

        // Adiciona um link para o histórico completo do produto, que é o contexto de onde esta movimentação veio.
        // Para isso, precisaríamos do produtoId, que não está diretamente no DTO da movimentação.
        // Por simplicidade e dado o contexto atual de listagem por produto, os links principais serão na CollectionModel.

        return model;
    }

    /**
     * Converte uma lista de {@link MovimentacaoProdutoResponseDTO} em um {@link CollectionModel} de
     * {@link MovimentacaoProdutoModel}, adicionando links HATEOAS para a coleção.
     *
     * @param entities A lista de DTOs de resposta da movimentação.
     * @param produtoId O ID do produto ao qual as movimentações pertencem.
     * @return Um CollectionModel de MovimentacaoProdutoModel com links.
     */
    public CollectionModel<MovimentacaoProdutoModel> toCollectionModel(List<MovimentacaoProdutoResponseDTO> entities, Long produtoId) {
        List<MovimentacaoProdutoModel> movimentacaoModels = entities.stream()
                .map(this::toModel) // Reutiliza o toModel para cada item
                .collect(Collectors.toList());

        CollectionModel<MovimentacaoProdutoModel> collectionModel = CollectionModel.of(movimentacaoModels);

        // Link self para a coleção de movimentações do produto
        collectionModel.add(linkTo(methodOn(EstoqueProdutoController.class).listarMovimentacoesPorProduto(produtoId)).withSelfRel());
        // Link para o produto (se houver um endpoint para o produto em si)
        // collectionModel.add(linkTo(methodOn(ProdutoController.class).findById(produtoId)).withRel("produto"));
        // Link para listar todos os estoques do produto
        collectionModel.add(linkTo(methodOn(EstoqueProdutoController.class).listarEstoquesPorProduto(produtoId)).withRel("estoques-do-produto"));
        // Link para ajustar o estoque físico do produto
        collectionModel.add(linkTo(methodOn(EstoqueProdutoController.class).ajustarEstoqueFisico(null)).withRel("ajustar-estoque-fisico"));


        return collectionModel;
    }
}
