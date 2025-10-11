package com.dcriar.api.dto.request.production;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

/**
 * DTO para solicitar a simulação de uma produção baseada em corte.
 */
@Data
@Builder
public class SimulacaoCorteRequestDTO {

    /**
     * O ID do produto para o qual a simulação de corte será realizada.
     */
    @NotNull
    @Schema(description = "ID do produto a ser simulado.", example = "2")
    private Long produtoId;

    /**
     * A quantidade de unidades do produto que se deseja produzir.
     */
    @NotNull
    @Positive
    @Schema(description = "Quantidade de unidades a serem produzidas.", example = "1000")
    private Integer quantidade;
}
