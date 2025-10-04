package com.dcriar.api.hateous.sales.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Modelo de Recurso HATEOAS para uma Venda (Sale).
 */
@Getter
@Setter
@Relation(collectionRelation = "sales", itemRelation = "sale")
public class SaleModel extends RepresentationModel<SaleModel> {

    private Long id;
    private OffsetDateTime saleDate;
    private String nomeCanalVenda;
    private BigDecimal totalAmount;
    private List<SaleItemModel> items;
}
