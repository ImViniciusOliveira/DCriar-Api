package com.dcriar.api.hateous.sales.model;

import com.dcriar.api.dto.response.sales.SaleItemResponseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;

/**
 * Modelo de Recurso HATEOAS para um item de venda.
 */
@Getter
@Setter
@Builder
@Relation(collectionRelation = "items", itemRelation = "item")
public class SaleItemModel extends RepresentationModel<SaleItemModel> {

    private Long id;
    private Long produtoId;
    private String produtoSku;
    private String nomeProduto;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    public static SaleItemModel fromDto(SaleItemResponseDTO dto) {
        return SaleItemModel.builder()
                .id(dto.getId())
                .produtoId(dto.getProdutoId())
                .produtoSku(dto.getProdutoSku())
                .nomeProduto(dto.getNomeProduto())
                .quantity(dto.getQuantity())
                .unitPrice(dto.getUnitPrice())
                .totalPrice(dto.getTotalPrice())
                .build();
    }
}
