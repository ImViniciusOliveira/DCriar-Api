package com.dcriar.api.hateous.assembler;

import com.dcriar.api.controller.product.ProdutoController;
import com.dcriar.api.controller.production.OrdemDeProducaoController;
import com.dcriar.api.dto.response.production.OrdemDeCorteResponseDTO;
import com.dcriar.api.hateous.model.OrdemDeCorteModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

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

    /**
     * Converte um {@link OrdemDeCorteResponseDTO} em um {@link OrdemDeCorteModel} e adiciona links HATEOAS.
     *
     * @param dto O DTO de resposta da ordem de corte.
     * @return O modelo HATEOAS da ordem de corte com links.
     */
    @Override
    @NonNull
    public OrdemDeCorteModel toModel(@NonNull OrdemDeCorteResponseDTO dto) {
        OrdemDeCorteModel model = OrdemDeCorteModel.fromDto(dto);

        // Link self para o recurso de ordem de corte específico (assumindo que GET /{id} existirá)
        model.add(linkTo(methodOn(OrdemDeProducaoController.class).buscarOrdemDeCortePorId(model.getId())).withSelfRel());
        // Link para o produto associado
        model.add(linkTo(methodOn(ProdutoController.class).findById(model.getProdutoId())).withRel("produto"));
        // Link para a ação de excluir a ordem de corte
        model.add(linkTo(methodOn(OrdemDeProducaoController.class).excluirOrdemDeCorte(model.getId())).withRel("excluir-ordem-de-corte"));
        // Link para a coleção de ordens de corte
        model.add(linkTo(methodOn(OrdemDeProducaoController.class).listarOrdensDeCorte()).withRel("ordens-de-corte"));


        // Não é possível adicionar link para lotePrincipalId pois não há LoteController
        // model.add(linkTo(methodOn(LoteController.class).findById(model.getLotePrincipalId())).withRel("lote-principal"));

        return model;
    }

    /**
     * Converte uma lista de {@link OrdemDeCorteResponseDTO} em um {@link CollectionModel} de
     * {@link OrdemDeCorteModel}, adicionando links HATEOAS para a coleção.
     *
     * @param entities A lista de DTOs de resposta da ordem de corte.
     * @return Um CollectionModel de OrdemDeCorteModel com links.
     */
    @Override
    @NonNull
    public CollectionModel<OrdemDeCorteModel> toCollectionModel(@NonNull Iterable<? extends OrdemDeCorteResponseDTO> entities) {
        List<OrdemDeCorteModel> ordemDeCorteModels = StreamSupport.stream(entities.spliterator(), false)
                .map(this::toModel)
                .collect(Collectors.toList());

        CollectionModel<OrdemDeCorteModel> collectionModel = CollectionModel.of(ordemDeCorteModels);

        // Link self para a coleção de ordens de corte
        collectionModel.add(linkTo(methodOn(OrdemDeProducaoController.class).listarOrdensDeCorte()).withSelfRel());
        // Link para a ação de criar uma nova ordem de corte
        collectionModel.add(linkTo(methodOn(OrdemDeProducaoController.class).processarOrdemDeCorte(null)).withRel("criar-ordem-de-corte"));

        return collectionModel;
    }
}