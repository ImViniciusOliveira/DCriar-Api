package com.dcriar.api.dto.response.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO para enviar os dados de dimensões (largura e comprimento) nas respostas da API.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DimensoesResponseDTO {

    @Schema(description = "Largura unitária do item em centímetros.", example = "20.0")
    private BigDecimal largura;

    @Schema(description = "Comprimento unitário do item em centímetros.", example = "30.0")
    private BigDecimal comprimento;
}
