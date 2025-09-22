package com.dcriar.api.dto.request.product;

import com.dcriar.api.validation.annotation.ValidAjusteEstoque;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidAjusteEstoque
public class AjusteEstoqueRequestDTO {

    @Schema(description = "O ID do produto cujo estoque será ajustado.", example = "1")
    private Long produtoId;

    @Schema(description = "O ID do canal de venda onde o estoque será ajustado.", example = "2")
    private Long canalVendaId;

    @Schema(description = "A quantidade a ser ajustada. Positivo para adicionar, negativo para remover.", example = "10")
    private Integer quantidade;
}
