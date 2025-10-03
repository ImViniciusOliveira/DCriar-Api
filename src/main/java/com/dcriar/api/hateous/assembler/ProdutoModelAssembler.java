package com.dcriar.api.hateous.assembler;

import com.dcriar.api.controller.product.EstoqueProdutoController; // Importar para links de estoque
import com.dcriar.api.controller.product.ProdutoController;
import com.dcriar.api.dto.response.product.ProdutoResponseDTO;
import com.dcriar.api.mapper.product.ProdutoMapper;
import com.dcriar.api.hateous.model.ProdutoModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ProdutoModelAssembler extends RepresentationModelAssemblerSupport<ProdutoResponseDTO, ProdutoModel> {

    private final ProdutoMapper mapper;

    public ProdutoModelAssembler(ProdutoMapper mapper) {
        super(ProdutoController.class, ProdutoModel.class);
        this.mapper = mapper;
    }

    @Override
    @NonNull
    public ProdutoModel toModel(@NonNull ProdutoResponseDTO dto) {
        // 1. Converte o DTO de resposta para o modelo HATEOAS.
        ProdutoModel model = mapper.toModel(dto);

        // 2. Adiciona os links HATEOAS.
        model.add(linkTo(methodOn(ProdutoController.class).findById(dto.getId())).withSelfRel());
        model.add(linkTo(methodOn(ProdutoController.class).findAll()).withRel("produtos"));

        // Links para recursos relacionados ao produto
        model.add(linkTo(methodOn(EstoqueProdutoController.class).listarEstoquesPorProduto(dto.getId())).withRel("estoques-do-produto"));
        model.add(linkTo(methodOn(EstoqueProdutoController.class).listarMovimentacoesPorProduto(dto.getId())).withRel("historico-movimentacoes"));

        // Links para ações sobre o produto
        model.add(linkTo(methodOn(ProdutoController.class).update(dto.getId(), null)).withRel("atualizar-produto"));
        model.add(linkTo(methodOn(ProdutoController.class).deleteById(dto.getId())).withRel("deletar-produto"));


        return model;
    }
}
