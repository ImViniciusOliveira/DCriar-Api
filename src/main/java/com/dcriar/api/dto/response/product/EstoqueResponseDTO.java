package com.dcriar.api.dto.response.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * DTO para enviar os dados do estoque de um produto acabado como resposta da API.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstoqueResponseDTO {

    @Schema(description = "ID único do registro de estoque.", example = "1")
    private Long id;

    @Schema(description = "ID do produto.", example = "1")
    private Long produtoId;

    @Schema(description = "Nome do produto.", example = "Etiqueta Redonda Kraft 5x5cm")
    private String nomeProduto;

    @Schema(description = "ID do canal de venda.", example = "2")
    private Long canalVendaId;

    @Schema(description = "Nome do canal de venda.", example = "SHOPEE")
    private String nomeCanalVenda;

    @Schema(description = "Quantidade do produto disponível neste canal.", example = "50")
    private Integer quantidade;
}
