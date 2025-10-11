package com.dcriar.api.hateous.production.assembler;

import com.dcriar.api.controller.production.OrdemDeProducaoController;
import com.dcriar.api.dto.response.production.OrdemDeProducaoResponseDTO;
import com.dcriar.api.hateous.production.model.OrdemDeProducaoModel;
import com.dcriar.api.mapper.production.OrdemDeProducaoMapper;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Assembler para converter OrdemDeProducaoResponseDTO em um OrdemDeProducaoModel com links HATEOAS.
 */
@Component
public class OrdemDeProducaoModelAssembler extends RepresentationModelAssemblerSupport<OrdemDeProducaoResponseDTO, OrdemDeProducaoModel> {

    private final OrdemDeProducaoMapper mapper;

    public OrdemDeProducaoModelAssembler(OrdemDeProducaoMapper mapper) {
        super(OrdemDeProducaoController.class, OrdemDeProducaoModel.class);
        this.mapper = mapper;
    }

    @Override
    @NonNull
    public OrdemDeProducaoModel toModel(@NonNull OrdemDeProducaoResponseDTO dto) {
        OrdemDeProducaoModel model = mapper.toModel(dto);

        model.add(linkTo(methodOn(OrdemDeProducaoController.class).buscarPorId(dto.getId())).withSelfRel());
        model.add(linkTo(methodOn(OrdemDeProducaoController.class).listarTodas()).withRel("ordens-de-producao"));

        return model;
    }

    public ResponseEntity<OrdemDeProducaoModel> toCreatedResponseEntity(@NonNull OrdemDeProducaoResponseDTO dto) {
        OrdemDeProducaoModel model = toModel(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(dto.getId())
                .toUri();

        return ResponseEntity.created(location).body(model);
    }
}
