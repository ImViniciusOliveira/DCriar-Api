package com.dcriar.api.hateous.assembler;

import com.dcriar.api.controller.product.EstoqueProdutoController;
import com.dcriar.api.controller.product.ProdutoController; // Importar ProdutoController
import com.dcriar.api.dto.response.product.EstoqueResponseDTO;
import com.dcriar.api.hateous.model.EstoqueProdutoModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Assembler responsável por converter {@link EstoqueResponseDTO} em {@link EstoqueProdutoModel}
 * e adicionar os links HATEOAS apropriados.
 */
@Component
public class EstoqueProdutoModelAssembler extends RepresentationModelAssemblerSupport<EstoqueResponseDTO, EstoqueProdutoModel> {

    public EstoqueProdutoModelAssembler() {
        super(EstoqueProdutoController.class, EstoqueProdutoModel.class);
    }

    /**
     * Converte um {@link EstoqueResponseDTO} em um {@link EstoqueProdutoModel} e adiciona links HATEOAS.
     *
     * @param dto O DTO de resposta do estoque.
     * @return O modelo HATEOAS do estoque com links.
     */
    @Override
    @NonNull
    public EstoqueProdutoModel toModel(@NonNull EstoqueResponseDTO dto) {
        EstoqueProdutoModel model = EstoqueProdutoModel.fromDto(dto);

        // Link self para o recurso de estoque específico
        model.add(linkTo(methodOn(EstoqueProdutoController.class).consultarEstoque(model.getProdutoId(), model.getCanalVendaId())).withSelfRel());
        // Link para listar todos os estoques do produto
        model.add(linkTo(methodOn(EstoqueProdutoController.class).listarEstoquesPorProduto(model.getProdutoId())).withRel("estoques-do-produto"));
        // Link para ajustar o estoque no canal
        model.add(linkTo(methodOn(EstoqueProdutoController.class).ajustarEstoqueCanal(null)).withRel("ajustar-estoque-canal"));
        // Link para o produto pai
        model.add(linkTo(methodOn(ProdutoController.class).findById(model.getProdutoId())).withRel("produto"));

        return model;
    }

    /**
     * Converte uma lista de {@link EstoqueResponseDTO} em um {@link CollectionModel} de {@link EstoqueProdutoModel},
     * adicionando links HATEOAS para cada item e para a coleção.
     *
     * @param entities A lista de DTOs de resposta do estoque.
     * @param produtoId O ID do produto ao qual os estoques pertencem.
     * @return Um CollectionModel de EstoqueProdutoModel com links.
     */
    public CollectionModel<EstoqueProdutoModel> toCollectionModel(List<EstoqueResponseDTO> entities, Long produtoId) {
        List<EstoqueProdutoModel> estoqueModels = entities.stream()
                .map(this::toModel) // Reutiliza o toModel para cada item
                .collect(Collectors.toList());

        CollectionModel<EstoqueProdutoModel> collectionModel = CollectionModel.of(estoqueModels);

        // Link self para a coleção
        collectionModel.add(linkTo(methodOn(EstoqueProdutoController.class).listarEstoquesPorProduto(produtoId)).withSelfRel());
        // Link para ajustar o estoque físico
        collectionModel.add(linkTo(methodOn(EstoqueProdutoController.class).ajustarEstoqueFisico(null)).withRel("ajustar-estoque-fisico"));
        // Link para o histórico de movimentações do produto
        collectionModel.add(linkTo(methodOn(EstoqueProdutoController.class).listarMovimentacoesPorProduto(produtoId)).withRel("historico-movimentacoes"));
        // Link para o produto pai
        collectionModel.add(linkTo(methodOn(ProdutoController.class).findById(produtoId)).withRel("produto"));

        return collectionModel;
    }
}
