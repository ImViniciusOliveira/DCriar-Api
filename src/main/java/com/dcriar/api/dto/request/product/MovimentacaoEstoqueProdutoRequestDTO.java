package com.dcriar.api.dto.request.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

/**
 * DTO para requisições de criação/atualização de movimentação de estoque de produto acabado.
 * <p>
 * Centraliza os dados necessários para registrar uma movimentação no livro-razão do estoque.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimentacaoEstoqueProdutoRequestDTO {

    /**
     * ID do produto associado à movimentação.
     */
    @NotNull
    @Schema(description = "ID do produto.", example = "1")
    private Long produtoId;

    /**
     * Tipo da movimentação (ex: ENTRADA_PRODUCAO, SAIDA_VENDA).
     */
    @NotNull
    @Schema(description = "Tipo da movimentação.", example = "ENTRADA_PRODUCAO")
    private String tipo;

    /**
     * Quantidade movimentada. Positiva para entradas, negativa para saídas.
     */
    @NotNull
    @Schema(description = "Quantidade movimentada.", example = "10")
    private Integer quantidade;

    /**
     * Motivo ou observação da movimentação.
     */
    @Schema(description = "Motivo ou observação.", example = "Ajuste manual")
    private String motivo;
}