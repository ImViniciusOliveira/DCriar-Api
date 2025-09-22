package com.dcriar.api.dto.request.sales;

import com.dcriar.api.validation.annotation.ValidSaleItemRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidSaleItemRequest
public class SaleItemRequestDTO {

    @Schema(description = "O ID do produto que está a ser vendido.", example = "1")
    private Long produtoId;

    @Schema(description = "A quantidade de unidades do produto vendidas.", example = "2")
    private Integer quantity;
}
