package com.dcriar.api.dto.request.sales;

import com.dcriar.api.validation.annotation.ValidSaleRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

/**
 * DTO para receber os dados de uma nova venda a ser registada.
 * <p>
 * Representa o "carrinho de compras" que o frontend envia para a API,
 * contendo o canal da venda e uma lista de itens vendidos.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidSaleRequest
public class SaleRequestDTO {

    @Schema(description = "O ID do canal de venda onde a transação ocorreu.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long canalVendaId;

    @Schema(description = "A lista de itens vendidos.", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<@Valid SaleItemRequestDTO> items;
}

