package com.dcriar.api.dto.request.production;

import com.dcriar.api.validation.annotation.ValidMargensRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidMargensRequest
public class MargensRequestDTO {

    @Schema(description = "Margem superior adicionada ao corte, em centímetros.", example = "2.0")
    private BigDecimal superior;

    @Schema(description = "Margem inferior adicionada ao corte, em centímetros.", example = "2.0")
    private BigDecimal inferior;

    @Schema(description = "Margem esquerda adicionada ao corte, em centímetros.", example = "1.5")
    private BigDecimal esquerda;

    @Schema(description = "Margem direita adicionada ao corte, em centímetros.", example = "1.5")
    private BigDecimal direita;
}