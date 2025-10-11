package com.dcriar.api.dto.response.production;

import com.dcriar.domain.stock.entity.enums.UnidadeDeMedida;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO para a resposta da simulação de uma produção baseada em consumo direto.
 */
@Data
@Builder
public class SimulacaoConsumoDiretoResponseDTO {

    @Schema(description = "Consumo total estimado de matéria-prima, na unidade de consumo padrão do material.", example = "10.0")
    private BigDecimal consumoTotalEstimado;

    @Schema(description = "Unidade de medida do consumo estimado.", example = "LITRO")
    private UnidadeDeMedida unidadeDeConsumo;
}
