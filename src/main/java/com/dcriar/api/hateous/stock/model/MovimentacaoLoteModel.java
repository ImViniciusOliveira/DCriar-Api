package com.dcriar.api.hateous.stock.model;

import com.dcriar.domain.stock.entity.enums.TipoMovimentacao;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Modelo de Recurso HATEOAS para uma Movimentação de Lote de Matéria-Prima.
 */
@Getter
@Setter
@Relation(collectionRelation = "movimentacoes", itemRelation = "movimentacao")
public class MovimentacaoLoteModel extends RepresentationModel<MovimentacaoLoteModel> {

    private Long id;
    private OffsetDateTime data;
    private TipoMovimentacao tipo;
    private BigDecimal quantidade;
    private BigDecimal custoPorUnidadeBase;
    private String motivo;
}
