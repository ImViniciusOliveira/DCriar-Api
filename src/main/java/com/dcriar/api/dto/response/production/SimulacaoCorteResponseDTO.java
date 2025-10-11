package com.dcriar.api.dto.response.production;

import com.dcriar.domain.production.enums.ModoCalculo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO para a resposta da simulação de uma produção baseada em corte.
 */
@Data
@Builder
public class SimulacaoCorteResponseDTO {

    @Schema(description = "Modo de cálculo sugerido ou utilizado na simulação.")
    private ModoCalculo modoCalculo;

    @Schema(description = "Largura final calculada para o corte (em cm).")
    private BigDecimal larguraFinalCm;

    @Schema(description = "Comprimento final calculado para o corte (em cm).")
    private BigDecimal comprimentoFinalCm;

    @Schema(description = "Consumo estimado de matéria-prima (em unidade de consumo do material).")
    private BigDecimal consumoEstimado;
}