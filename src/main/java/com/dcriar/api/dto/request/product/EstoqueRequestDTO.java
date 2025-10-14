package com.dcriar.api.dto.request.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

/**
 * DTO para requisições de criação/atualização de estoque de produto em canal de venda.
 * <p>
 * Centraliza os dados necessários para manipulação de estoque.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstoqueRequestDTO {

    /**
     * ID do produto associado ao estoque.
     */
    @NotNull
    @Schema(description = "ID do produto.", example = "1")
    private Long produtoId;

    /**
     * ID do canal de venda onde o estoque está alocado.
     */
    @NotNull
    @Schema(description = "ID do canal de venda.", example = "2")
    private Long canalVendaId;

    /**
     * Quantidade de unidades disponíveis.
     */
    @NotNull
    @Positive
    @Schema(description = "Quantidade de unidades.", example = "100")
    private Integer quantidade;
}
