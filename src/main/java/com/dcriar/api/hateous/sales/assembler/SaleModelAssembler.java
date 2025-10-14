package com.dcriar.api.hateous.sales.assembler;

import com.dcriar.api.controller.sales.SaleController;
import com.dcriar.api.dto.response.sales.SaleResponseDTO;
import com.dcriar.api.hateous.sales.model.SaleItemModel;
import com.dcriar.api.hateous.sales.model.SaleModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Assembler principal para o recurso de Venda (Sale).
 * Converte {@link SaleResponseDTO} em {@link SaleModel} e constrói as respostas HATEOAS.
 */
@Component
public class SaleModelAssembler extends RepresentationModelAssemblerSupport<SaleResponseDTO, SaleModel> {

    private final SaleItemModelAssembler saleItemModelAssembler;

    /**
     * Construtor que injeta as dependências necessárias e inicializa a superclasse corretamente.
     *
     * @param saleItemModelAssembler O assembler para os itens da venda, injetado pelo Spring.
     */
    @Autowired
    public SaleModelAssembler(SaleItemModelAssembler saleItemModelAssembler) {
        super(SaleController.class, SaleModel.class);
        this.saleItemModelAssembler = saleItemModelAssembler;
    }

    /**
     * Converte um {@link SaleResponseDTO} em {@link SaleModel},
     * adicionando links HATEOAS e montando os itens da venda.
     * <p>
     * Links adicionados:
     * <ul>
     *   <li>Auto (self)</li>
     *   <li>Outros links relevantes do recurso de venda</li>
     * </ul>
     * Exemplo de uso:
     * <pre>
     *   SaleModel model = saleModelAssembler.toModel(saleResponseDTO);
     * </pre>
     * @param dto DTO de resposta da venda
     * @return Modelo HATEOAS enriquecido
     */
    @Override
    @NonNull
    public SaleModel toModel(@NonNull SaleResponseDTO dto) {
        SaleModel model = instantiateModel(dto);

        model.setId(dto.getId());
        model.setSaleDate(dto.getSaleDate());
        model.setNomeCanalVenda(dto.getNomeCanalVenda());
        model.setTotalAmount(dto.getTotalAmount());

        if (dto.getItems() != null) {
            List<SaleItemModel> itemModels = dto.getItems().stream()
                    .map(saleItemModelAssembler::toModel)
                    .collect(Collectors.toList());
            model.setItems(itemModels);
        } else {
            model.setItems(Collections.emptyList());
        }

        model.add(linkTo(methodOn(SaleController.class).findById(dto.getId())).withSelfRel());
        model.add(linkTo(methodOn(SaleController.class).findAll()).withRel("sales"));

        return model;
    }

    public ResponseEntity<SaleModel> toCreatedResponseEntity(@NonNull SaleResponseDTO dto) {
        SaleModel model = toModel(dto);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(dto.getId())
                .toUri();

        return ResponseEntity.created(location).body(model);
    }
}
