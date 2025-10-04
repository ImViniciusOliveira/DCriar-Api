package com.dcriar.api.hateous.production.assembler;

import com.dcriar.api.controller.product.ProdutoController;
import com.dcriar.api.controller.production.OrdemDeProducaoController;
import com.dcriar.api.dto.response.production.OrdemDeCorteResponseDTO;
import com.dcriar.api.hateous.production.model.OrdemDeCorteModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Assembler responsável por converter {@link OrdemDeCorteResponseDTO} em {@link OrdemDeCorteModel}
 * e adicionar os links HATEOAS apropriados.
 */
@Component
public class OrdemDeCorteModelAssembler extends RepresentationModelAssemblerSupport<OrdemDeCorteResponseDTO, OrdemDeCorteModel> {

    public OrdemDeCorteModelAssembler() {
        super(OrdemDeProducaoController.class, OrdemDeCorteModel.class);
    }

    @Override
    @NonNull
    public OrdemDeCorteModel toModel(@NonNull OrdemDeCorteResponseDTO dto) {
        OrdemDeCorteModel model = OrdemDeCorteModel.fromDto(dto);

        model.add(linkTo(methodOn(OrdemDeProducaoController.class).buscarOrdemDeCortePorId(model.getId())).withSelfRel());
        model.add(linkTo(methodOn(ProdutoController.class).findById(model.getProdutoId())).withRel("produto"));
        model.add(linkTo(methodOn(OrdemDeProducaoController.class).excluirOrdemDeCorte(model.getId())).withRel("excluir-ordem-de-corte"));
        model.add(linkTo(methodOn(OrdemDeProducaoController.class).listarOrdensDeCorte()).withRel("ordens-de-corte"));

        return model;
    }

    @Override
    @NonNull
    public CollectionModel<OrdemDeCorteModel> toCollectionModel(@NonNull Iterable<? extends OrdemDeCorteResponseDTO> entities) {
        List<OrdemDeCorteModel> ordemDeCorteModels = StreamSupport.stream(entities.spliterator(), false)
                .map(this::toModel)
                .collect(Collectors.toList());

        CollectionModel<OrdemDeCorteModel> collectionModel = CollectionModel.of(ordemDeCorteModels);

        collectionModel.add(linkTo(methodOn(OrdemDeProducaoController.class).listarOrdensDeCorte()).withSelfRel());
        collectionModel.add(linkTo(methodOn(OrdemDeProducaoController.class).processarOrdemDeCorte(null)).withRel("criar-ordem-de-corte"));

        return collectionModel;
    }

    /**
     * Constrói a resposta HTTP completa para a criação de um novo recurso, incluindo o status 201,
     * o header Location e o corpo HATEOAS.
     *
     * @param dto O DTO do recurso recém-criado.
     * @return Um ResponseEntity<OrdemDeCorteModel> pronto para ser retornado pelo controller.
     */
    public ResponseEntity<OrdemDeCorteModel> toCreatedResponseEntity(@NonNull OrdemDeCorteResponseDTO dto) {
        OrdemDeCorteModel model = toModel(dto);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(dto.getId())
                .toUri();

        return ResponseEntity.created(location).body(model);
    }
}
