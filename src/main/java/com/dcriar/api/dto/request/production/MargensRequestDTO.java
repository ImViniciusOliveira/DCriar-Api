package com.dcriar.api.dto.request.production;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO para encapsular os valores das margens de corte.
 * <p>
 * Usado dentro do {@link OrdemDeCorteRequestDTO} quando o modo de cálculo é AUTOMATICO.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MargensRequestDTO {

    @PositiveOrZero(message = "A margem deve ser um valor positivo ou zero.")
    @Schema(description = "Margem superior em cm (opcional).", example = "2.0")
    private BigDecimal superior;

    @PositiveOrZero(message = "A margem deve ser um valor positivo ou zero.")
    @Schema(description = "Margem inferior em cm (opcional).", example = "2.0")
    private BigDecimal inferior;

    @PositiveOrZero(message = "A margem deve ser um valor positivo ou zero.")
    @Schema(description = "Margem esquerda em cm (opcional).", example = "1.0")
    private BigDecimal esquerda;

    @PositiveOrZero(message = "A margem deve ser um valor positivo ou zero.")
    @Schema(description = "Margem direita em cm (opcional).", example = "1.0")
    private BigDecimal direita;
}
