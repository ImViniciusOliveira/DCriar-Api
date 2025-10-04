package com.dcriar.api.hateous.stock.model;

import com.dcriar.domain.stock.entity.enums.UnidadeDeMedida;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Modelo de Recurso HATEOAS para um Lote de Matéria-Prima.
 */
@Getter
@Setter
@Relation(collectionRelation = "lotes-materia-prima", itemRelation = "lote-materia-prima")
public class LoteMateriaPrimaModel extends RepresentationModel<LoteMateriaPrimaModel> {

    private Long id;
    private Long tipoMateriaPrimaId; // Essencial para o link HATEOAS
    private String nomeTipoMateriaPrima;
    private UnidadeDeMedida unidadeDeEstoque;
    private BigDecimal saldoEstoque;
    private Map<String, Object> atributos;
    private Long loteDeOrigemId;
}
