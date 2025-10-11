package com.dcriar.api.dto.request.production;

import com.dcriar.api.validation.annotation.ValidOrdemDeCorteRequest;
import com.dcriar.domain.production.enums.ModoCalculo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO para a criação de uma nova Ordem de Produção do tipo CORTE.
 */
@Data
@Builder
@ValidOrdemDeCorteRequest
public class OrdemDeCorteRequestDTO {

    @NotNull
    @Schema(description = "ID do produto a ser fabricado (deve ser um produto de matéria-prima geométrica).", example = "2")
    private Long produtoId;

    @NotNull
    @Schema(description = "ID do lote de matéria-prima principal a ser consumido.", example = "2")
    private Long lotePrincipalId;

    @Schema(description = "ID do canal de venda de destino do estoque (opcional).", example = "5")
    private Long canalVendaDestinoId;

    @NotNull
    @Positive
    @Schema(description = "Quantidade de unidades do produto a serem produzidas.", example = "10")
    private Integer quantidadeProduzida;

    @NotNull
    @Schema(description = "Modo de cálculo para o corte.", example = "AUTOMATICO")
    private ModoCalculo modoCalculo;

    @Schema(description = "Margens de segurança (em cm) a serem aplicadas.")
    private MargensRequestDTO margens;

    @Schema(description = "Largura final do corte em cm (usado no modo MANUAL).", example = "80.0")
    private BigDecimal larguraFinalCm;

    @Schema(description = "Comprimento final do corte em cm (usado no modo MANUAL).", example = "120.0")
    private BigDecimal comprimentoFinalCm;

    @Schema(description = "Motivo ou referência para a ordem.", example = "Pedido Cliente #456")
    private String motivo;
}
