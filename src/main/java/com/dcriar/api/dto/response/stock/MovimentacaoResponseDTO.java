package com.dcriar.api.dto.response.stock;

import com.dcriar.domain.stock.entity.enuns.TipoMovimentacao;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * DTO para enviar os dados de uma única movimentação de estoque como resposta da API.
 * <p>
 * Representa uma linha do "Livro-Razão" (histórico) de um lote de matéria-prima.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimentacaoResponseDTO {

    @Schema(description = "ID único da movimentação.", example = "1")
    private Long id;

    @Schema(description = "Data e hora em que a movimentação foi registada.")
    private OffsetDateTime data;

    @Schema(description = "O tipo da movimentação.", example = "ENTRADA_COMPRA")
    private TipoMovimentacao tipo;

    @Schema(description = "A quantidade que foi movimentada. Positiva para entradas, negativa para saídas.", example = "50.00")
    private BigDecimal quantidade;

    /**
     * O custo calculado por unidade base (ex: R$/cm², R$/ml) no momento da entrada.
     * Este campo só terá valor para movimentações do tipo ENTRADA_COMPRA.
     */
    @Schema(description = "O custo por unidade base no momento da entrada (ex: R$/cm²). Será nulo para outras movimentações.", example = "0.0007")
    private BigDecimal custoPorUnidadeBase;

    @Schema(description = "O motivo ou observação registado para a movimentação.", example = "Entrada inicial do lote no sistema.")
    private String motivo;
}

