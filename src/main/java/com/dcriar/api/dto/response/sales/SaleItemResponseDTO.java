package com.dcriar.api.dto.response.sales;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO para representar um item individual dentro da resposta de uma venda.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleItemResponseDTO {

    @Schema(description = "ID único do item da venda.", example = "1")
    private Long id;

    @Schema(description = "SKU do produto vendido.", example = "ETQ-KFT-RD-50")
    private String produtoSku;

    @Schema(description = "Nome do produto vendido.", example = "Etiqueta Redonda Kraft 5x5cm")
    private String nomeProduto;

    @Schema(description = "Quantidade de unidades do produto vendidas.", example = "2")
    private Integer quantity;

    @Schema(description = "Preço unitário do produto no momento da venda.", example = "25.00")
    private BigDecimal unitPrice;

    @Schema(description = "Preço total para este item (quantidade * preço unitário).", example = "50.00")
    private BigDecimal totalPrice;
}
