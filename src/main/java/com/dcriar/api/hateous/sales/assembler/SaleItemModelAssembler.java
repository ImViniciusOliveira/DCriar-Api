package com.dcriar.api.hateous.sales.assembler;

import com.dcriar.api.controller.product.ProdutoController;
import com.dcriar.api.dto.response.sales.SaleItemResponseDTO;
import com.dcriar.api.hateous.sales.model.SaleItemModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Assembler responsável por converter {@link SaleItemResponseDTO} em {@link SaleItemModel}
 * e adicionar os links HATEOAS apropriados para um item de venda.
 */
@Component
public class SaleItemModelAssembler extends RepresentationModelAssemblerSupport<SaleItemResponseDTO, SaleItemModel> {

    public SaleItemModelAssembler() {
        super(SaleItemModel.class, SaleItemModel.class); // Contexto é o próprio item
    }

    @Override
    @NonNull
    public SaleItemModel toModel(@NonNull SaleItemResponseDTO dto) {
        SaleItemModel model = SaleItemModel.fromDto(dto);

        // Adiciona um link para o recurso do produto associado a este item de venda.
        if (model.getProdutoId() != null) {
            model.add(linkTo(methodOn(ProdutoController.class).findById(model.getProdutoId())).withRel("produto"));
        }

        return model;
    }
}
