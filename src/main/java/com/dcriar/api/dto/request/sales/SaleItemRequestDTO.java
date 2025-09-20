package com.dcriar.api.dto.request.sales;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

/**
 * DTO para receber os dados de um item individual dentro de uma venda.
 * <p>
 * Representa uma "linha do recibo" na requisição, especificando o produto
 * e a quantidade que foram vendidos.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleItemRequestDTO {

    /**
     * O ID do produto que está a ser vendido.
     */
    @NotNull(message = "O ID do produto é obrigatório.")
    @Schema(description = "O ID do produto que está a ser vendido.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long produtoId;

    /**
     * A quantidade de unidades do produto que foram vendidas.
     */
    @NotNull(message = "A quantidade é obrigatória.")
    @Positive(message = "A quantidade deve ser um valor positivo.")
    @Schema(description = "A quantidade de unidades do produto vendidas.", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;
}

