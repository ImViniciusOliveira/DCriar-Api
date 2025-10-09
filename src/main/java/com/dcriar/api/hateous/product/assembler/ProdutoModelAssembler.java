package com.dcriar.api.hateous.product.assembler;

import com.dcriar.api.controller.product.EstoqueProdutoController;
import com.dcriar.api.controller.product.ProdutoController;
import com.dcriar.api.controller.stock.TipoMateriaPrimaController;
import com.dcriar.api.dto.response.product.ProdutoResponseDTO;
import com.dcriar.api.mapper.product.ProdutoMapper;
import com.dcriar.api.hateous.product.model.ProdutoModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

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
        ProdutoModel model = mapper.toModel(dto);

        // Links do próprio recurso
        model.add(linkTo(methodOn(ProdutoController.class).findById(dto.getId())).withSelfRel());
        model.add(linkTo(methodOn(ProdutoController.class).update(dto.getId(), null)).withRel("atualizar-produto"));
        model.add(linkTo(methodOn(ProdutoController.class).deleteById(dto.getId())).withRel("deletar-produto"));

        // Links de navegação e descoberta
        model.add(linkTo(methodOn(ProdutoController.class).findAll()).withRel("produtos"));
        model.add(linkTo(methodOn(TipoMateriaPrimaController.class).findAll(null, null, null, null)).withRel("buscar-tipos-materia-prima"));

        // Links para recursos relacionados
        model.add(linkTo(methodOn(EstoqueProdutoController.class).listarEstoquesPorProduto(dto.getId())).withRel("estoques-do-produto"));
        model.add(linkTo(methodOn(EstoqueProdutoController.class).listarMovimentacoesPorProduto(dto.getId())).withRel("historico-movimentacoes"));

        return model;
    }

    public ResponseEntity<ProdutoModel> toCreatedResponseEntity(@NonNull ProdutoResponseDTO dto) {
        ProdutoModel model = toModel(dto);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(dto.getId())
                .toUri();

        return ResponseEntity.created(location).body(model);
    }
}
