package com.dcriar.api.dto.response.product;

import com.dcriar.domain.product.entity.enuns.TipoMovimentacaoProduto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimentacaoProdutoResponseDTO {

    @Schema(description = "ID único da movimentação.", example = "1")
    private Long id;

    @Schema(description = "Data e hora da movimentação.", example = "2025-09-22T03:13:23.522Z")
    private OffsetDateTime data;

    @Schema(description = "Tipo da movimentação do estoque.", example = "ENTRADA_PRODUCAO")
    private TipoMovimentacaoProduto tipo;

    @Schema(description = "Quantidade movimentada. Positiva para entradas, negativa para saídas.", example = "100")
    private Integer quantidade;

    @Schema(description = "Motivo ou observação da movimentação.", example = "Produzido via Ordem de Corte ID: #123")
    private String motivo;
}
