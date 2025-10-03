package com.dcriar.api.dto.response.production;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

/**
 * Data Transfer Object (DTO) que representa as dimensões finais de um produto.
 * <p>
 * Este DTO é usado para comunicar o tamanho final (largura e comprimento) de um
 * item após o processo de corte.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TamanhoFinalResponseDTO {

    /**
     * A largura final do produto em centímetros.
     */
    @Schema(description = "A largura final do produto em centímetros.", example = "50.0")
    private BigDecimal larguraCm;

    /**
     * O comprimento final do produto em centímetros.
     */
    @Schema(description = "O comprimento final do produto em centímetros.", example = "100.0")
    private BigDecimal comprimentoCm;
}
