package com.dcriar.api.dto.request.stock;

import com.dcriar.domain.stock.entity.enuns.TipoMovimentacao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO para receber os dados de uma nova movimentação de estoque a ser registada.
 * <p>
 * Este objeto serve como o "formulário" para a API, definindo os dados necessários
 * para registar uma saída de produção, uma perda ou um ajuste de inventário.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimentacaoRequestDTO {

    /**
     * O tipo da movimentação a ser registada (ex: SAIDA_PRODUCAO, PERDA_DESCARTE).
     */
    @NotNull(message = "O tipo da movimentação é obrigatório.")
    @Schema(description = "O tipo da movimentação a ser registada.", example = "SAIDA_PRODUCAO", requiredMode = Schema.RequiredMode.REQUIRED)
    private TipoMovimentacao tipo;

    /**
     * A quantidade a ser movimentada. Deve ser um valor negativo para saídas e perdas.
     */
    @NotNull(message = "A quantidade é obrigatória.")
    @Schema(description = "A quantidade a ser movimentada. Use um valor negativo para saídas (ex: -1.5).", example = "-1.50", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal quantidade;

    /**
     * Uma descrição textual que justifica a movimentação.
     */
    @Schema(description = "Um motivo ou observação para a movimentação.", example = "Uso na produção do pedido #123")
    private String motivo;
}
