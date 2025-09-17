package com.dcriar.api.dto.request.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para receber os dados de um ajuste manual de estoque de um produto acabado.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AjusteEstoqueRequestDTO {

    /**
     * O ID do produto cujo estoque será ajustado.
     */
    @NotNull(message = "O ID do produto é obrigatório.")
    @Schema(description = "O ID do produto cujo estoque será ajustado.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long produtoId;

    /**
     * O ID do canal de venda onde o estoque será ajustado.
     */
    @NotNull(message = "O ID do canal de venda é obrigatório.")
    @Schema(description = "O ID do canal de venda onde o estoque será ajustado.", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long canalVendaId;

    /**
     * A quantidade a ser ajustada. Pode ser um valor positivo para adicionar ao estoque
     * ou um valor negativo para remover do estoque.
     */
    @NotNull(message = "A quantidade do ajuste é obrigatória.")
    @Schema(description = "A quantidade a ser ajustada. Positivo para adicionar, negativo para remover.", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantidade;
}
