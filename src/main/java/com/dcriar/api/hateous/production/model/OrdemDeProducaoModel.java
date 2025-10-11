package com.dcriar.api.hateous.production.model;

import com.dcriar.domain.production.enums.ModoCalculo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Modelo de representação HATEOAS para uma Ordem de Produção.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@JsonRootName(value = "ordemDeProducao")
@Relation(collectionRelation = "ordensDeProducao", itemRelation = "ordemDeProducao")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrdemDeProducaoModel extends RepresentationModel<OrdemDeProducaoModel> {

    @Schema(description = "ID único da ordem de produção.")
    private Long id;

    @Schema(description = "ID do produto final fabricado.")
    private Long produtoId;

    @Schema(description = "Nome do produto final fabricado.")
    private String nomeProduto;

    @Schema(description = "Lista de IDs dos lotes de matéria-prima consumidos.")
    private List<Long> lotesConsumidosIds;

    @Schema(description = "Quantidade de unidades do produto que foram produzidas.")
    private Integer quantidadeProduzida;

    @Schema(description = "Modo de cálculo utilizado (relevante para ordens de corte).")
    private ModoCalculo modoCalculo;

    @Schema(description = "Data e hora em que a ordem foi criada.")
    private OffsetDateTime dataCriacao;

    @Schema(description = "Motivo ou referência para a ordem.")
    private String motivo;

    @Schema(description = "Largura final do corte em cm (se aplicável).", nullable = true)
    private BigDecimal larguraFinalCm;

    @Schema(description = "Comprimento final do corte em cm (se aplicável).", nullable = true)
    private BigDecimal comprimentoFinalCm;
}
