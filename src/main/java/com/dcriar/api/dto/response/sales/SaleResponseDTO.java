package com.dcriar.api.dto.response.sales;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * DTO para enviar os dados de uma Venda (Sale) finalizada como resposta da API.
 * <p>
 * Fornece uma representação completa da transação, incluindo todos os seus itens.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleResponseDTO {

    @Schema(description = "ID único da venda.", example = "1")
    private Long id;

    @Schema(description = "Data e hora em que a venda foi registada.")
    private OffsetDateTime saleDate;

    @Schema(description = "Nome do canal de venda onde a transação ocorreu.", example = "SHOPEE")
    private String nomeCanalVenda;

    @Schema(description = "O valor total da venda.", example = "99.90")
    private BigDecimal totalAmount;

    @Schema(description = "A lista de itens que foram vendidos nesta transação.")
    private List<SaleItemResponseDTO> items;
}