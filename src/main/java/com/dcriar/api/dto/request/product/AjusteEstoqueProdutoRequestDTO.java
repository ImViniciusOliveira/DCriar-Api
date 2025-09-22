package com.dcriar.api.dto.request.product;

import com.dcriar.api.validation.annotation.ValidAjusteEstoqueProduto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidAjusteEstoqueProduto
public class AjusteEstoqueProdutoRequestDTO {

    @Schema(description = "O ID do produto cujo estoque físico total será ajustado.", example = "1")
    private Long produtoId;

    @Schema(description = "A quantidade a ser ajustada. Positivo para adicionar, negativo para remover.", example = "-5")
    private Integer quantidade;

    @Schema(description = "O motivo da movimentação manual.", example = "Correção de inventário - contagem física.")
    private String motivo;
}
