package com.dcriar.api.dto.request.production;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class SimulacaoOrdemDeCorteRequestDTO {

    @NotNull
    private Long produtoId;

    @NotNull
    @Positive
    private Integer quantidade;
}
