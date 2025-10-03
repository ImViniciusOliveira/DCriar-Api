package com.dcriar.api.dto.request.sales;

import com.dcriar.api.validation.annotation.ValidSaleRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.*;

import java.util.List;

/**
 * Data Transfer Object (DTO) para registrar uma nova Venda.
 * <p>
 * Este DTO é utilizado para receber os dados de uma nova venda, incluindo o canal
 * onde a transação ocorreu e a lista de itens vendidos. A validação dos dados é
 * garantida pela anotação customizada {@link ValidSaleRequest}.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidSaleRequest
public class SaleRequestDTO {

    /**
     * O ID do canal de venda onde a transação ocorreu.
     */
    @Schema(description = "O ID do canal de venda onde a transação ocorreu.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long canalVendaId;

    /**
     * A lista de itens que compõem a venda.
     * <p>
     * A anotação {@code @Valid} garante que cada item da lista seja validado individualmente.
     */
    @Schema(description = "A lista de itens que compõem a venda.", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<@Valid SaleItemRequestDTO> items;
}
