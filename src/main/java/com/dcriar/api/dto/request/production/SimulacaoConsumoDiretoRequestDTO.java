package com.dcriar.api.dto.request.production;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) para solicitar a simulação de consumo de matéria-prima.
 * <p>
 * Utilizado para calcular a quantidade total de matéria-prima necessária para produzir
 * uma certa quantidade de um produto de consumo direto (líquidos, pós, etc.),
 * sem efetivamente criar uma ordem de produção ou movimentar o estoque.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulacaoConsumoDiretoRequestDTO {

    /**
     * O ID do produto para o qual a simulação será realizada.
     */
    @NotNull
    @Schema(description = "ID do produto a ser simulado.", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long produtoId;

    /**
     * A quantidade de unidades do produto que se deseja produzir.
     */
    @NotNull
    @Positive
    @Schema(description = "Quantidade de unidades a serem produzidas na simulação.", example = "200", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantidade;
}
