package com.dcriar.api.dto.response.product;

import com.dcriar.domain.product.entity.enuns.TipoMovimentacaoProduto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * DTO para enviar os dados de uma única movimentação do Estoque Mestre como resposta da API.
 * <p>
 * Representa uma linha do "Livro-Razão" (histórico) de um produto acabado.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimentacaoProdutoResponseDTO {

    @Schema(description = "ID único da movimentação.", example = "1")
    private Long id;

    @Schema(description = "Data e hora em que a movimentação foi registada.")
    private OffsetDateTime data;

    @Schema(description = "O tipo da movimentação.", example = "ENTRADA_PRODUCAO")
    private TipoMovimentacaoProduto tipo;

    @Schema(description = "A quantidade que foi movimentada. Positiva para entradas, negativa para saídas.", example = "100")
    private Integer quantidade;

    @Schema(description = "O motivo ou observação registado para a movimentação.", example = "Produzido via Ordem de Corte ID: #123")
    private String motivo;
}