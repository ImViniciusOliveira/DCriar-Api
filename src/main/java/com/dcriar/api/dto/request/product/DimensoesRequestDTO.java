package com.dcriar.api.dto.request.product;

import com.dcriar.api.validation.annotation.ValidDimensoesRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO para receber os dados de dimensões (largura e comprimento) nas requisições da API.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidDimensoesRequest
public class DimensoesRequestDTO {

    @Schema(description = "Largura unitária do item em centímetros.", example = "20.0")
    private BigDecimal larguraCm;

    @Schema(description = "Comprimento unitário do item em centímetros.", example = "30.0")
    private BigDecimal comprimentoCm;
}
