package com.dcriar.api.dto.request.production;

import com.dcriar.api.validation.annotation.ValidOrdemDeCorteRequest;
import com.dcriar.domain.production.enums.ModoCalculo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Data Transfer Object (DTO) para criar uma nova Ordem de Produção por Corte Geométrico.
 * <p>
 * Utilizado para produtos cuja matéria-prima é medida e cortada geometricamente (ex: metros, cm²).
 * Suporta dois modos de operação: AUTOMATICO, onde o sistema otimiza o corte, e MANUAL,
 * onde o usuário informa as dimensões exatas do material consumido.
 * A validação das regras de negócio é garantida pela anotação {@link ValidOrdemDeCorteRequest}.
 */
@Getter
@Setter
@ToString
@Builder
@ValidOrdemDeCorteRequest
public class OrdemDeCorteRequestDTO {

    @NotNull
    @Schema(description = "ID do produto a ser fabricado (deve ser um produto de matéria-prima geométrica).", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long produtoId;

    @NotNull
    @Schema(description = "ID do lote de matéria-prima principal a ser consumido.", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long lotePrincipalId;

    @Schema(description = "ID do canal de venda de destino do estoque (opcional). Se fornecido, o estoque produzido será alocado neste canal.", example = "5")
    private Long canalVendaDestinoId;

    @NotNull
    @Positive
    @Schema(description = "Quantidade de unidades do produto a serem produzidas.", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantidadeProduzida;

    @NotNull
    @Schema(description = "Modo de cálculo para o corte.", example = "AUTOMATICO", requiredMode = Schema.RequiredMode.REQUIRED)
    private ModoCalculo modoCalculo;

    @Schema(description = "Margens de segurança (em cm) a serem aplicadas (relevante no modo AUTOMATICO).")
    private MargensRequestDTO margens;

    @Schema(description = "Largura final do corte em cm (obrigatório no modo MANUAL).", example = "80.0")
    private BigDecimal larguraFinalCm;

    @Schema(description = "Comprimento final do corte em cm (obrigatório no modo MANUAL).", example = "120.0")
    private BigDecimal comprimentoFinalCm;

    @Schema(description = "Motivo ou referência para a ordem.", example = "Pedido Cliente #456")
    private String motivo;
}
