package com.dcriar.api.hateous.sales.model;

import com.dcriar.api.dto.response.sales.SaleItemResponseDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;

/**
 * Modelo de representação HATEOAS para um Item de Venda.
 * <p>
 * Representa uma única linha dentro de uma venda, detalhando o produto,
 * a quantidade e os valores envolvidos.
 */
@Getter
@Setter
@Builder
@JsonRootName(value = "item")
@Relation(collectionRelation = "items", itemRelation = "item")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SaleItemModel extends RepresentationModel<SaleItemModel> {

    @Schema(description = "ID único do item da venda.", example = "101")
    private Long id;

    @Schema(description = "ID do produto vendido.", example = "2")
    private Long produtoId;

    @Schema(description = "SKU do produto vendido.", example = "BNR-COM-120X80")
    private String produtoSku;

    @Schema(description = "Nome do produto vendido.", example = "Banner Comercial 1,20x0,80m")
    private String nomeProduto;

    @Schema(description = "Quantidade de unidades vendidas.", example = "5")
    private Integer quantity;

    @Schema(description = "Preço unitário do produto no momento da venda.", example = "25.10")
    private BigDecimal unitPrice;

    @Schema(description = "Preço total para este item (quantidade * preço unitário).", example = "125.50")
    private BigDecimal totalPrice;

    /**
     * Método de fábrica para converter um DTO de resposta em um modelo HATEOAS.
     *
     * @param dto O DTO de resposta do item de venda.
     * @return Uma nova instância de {@link SaleItemModel}.
     */
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
