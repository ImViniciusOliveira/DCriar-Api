package com.dcriar.api.hateous.stock.model;

import com.dcriar.domain.stock.entity.enums.UnidadeDeMedida;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

/**
 * Modelo de Recurso HATEOAS para um Tipo de Matéria-Prima.
 */
@Getter
@Setter
@Relation(collectionRelation = "tipos-materia-prima", itemRelation = "tipo-materia-prima")
public class TipoMateriaPrimaModel extends RepresentationModel<TipoMateriaPrimaModel> {

    private Long id;
    private String nome;
    private UnidadeDeMedida unidadeDeConsumo;
}
