package com.dcriar.api.dto.request.product;

import com.dcriar.domain.product.entity.enuns.TipoMovimentacaoProduto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para receber os dados de um ajuste manual no Estoque Físico Total de um produto acabado.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AjusteEstoqueProdutoRequestDTO {

    /**
     * O ID do produto cujo estoque físico total será ajustado.
     */
    @NotNull(message = "O ID do produto é obrigatório.")
    @Schema(description = "O ID do produto cujo estoque físico total será ajustado.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long produtoId;

    /**
     * A quantidade a ser ajustada. Positivo para adicionar (ex: devolução),
     * negativo para remover (ex: perda).
     */
    @NotNull(message = "A quantidade do ajuste é obrigatória.")
    @Schema(description = "A quantidade a ser ajustada. Positivo para adicionar, negativo para remover.", example = "-5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantidade;

    /**
     * A justificativa para a movimentação manual. Essencial para a auditoria.
     */
    @NotBlank(message = "O motivo é obrigatório para ajustes manuais.")
    @Schema(description = "A justificativa para a movimentação manual.", example = "Correção de inventário - contagem física.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String motivo;
}
