package com.dcriar.api.dto.request.product;

import com.dcriar.api.validation.annotation.ValidDimensoesRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @NotNull(message = "A largura é obrigatória.")
    @Positive(message = "A largura deve ser um valor positivo.")
    @Schema(description = "Largura unitária do item em centímetros.", example = "20.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal largura;

    @NotNull(message = "O comprimento é obrigatório.")
    @Positive(message = "O comprimento deve ser um valor positivo.")
    @Schema(description = "Comprimento unitário do item em centímetros.", example = "30.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal comprimento;
}
