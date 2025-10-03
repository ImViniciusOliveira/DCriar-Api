package com.dcriar.api.dto.response.production;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

/**
 * Data Transfer Object (DTO) que representa um corte realizado em uma bobina de matéria-prima.
 * <p>
 * Este DTO detalha as dimensões de uma faixa cortada, a quantidade produzida
 * e se o corte resultou em um produto final ou em um retalho.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorteRealizadoDTO {

    /**
     * A largura do corte em centímetros.
     */
    @Schema(description = "A largura do corte em centímetros.", example = "50.5")
    private BigDecimal larguraCm;

    /**
     * O comprimento do corte em centímetros.
     */
    @Schema(description = "O comprimento do corte em centímetros.", example = "100.0")
    private BigDecimal comprimentoCm;

    /**
     * A quantidade de faixas idênticas produzidas com estas dimensões.
     */
    @Schema(description = "A quantidade de faixas idênticas produzidas com estas dimensões.", example = "10")
    private int quantidade;

    /**
     * O tipo de resultado do corte.
     * Pode ser "PRODUTO" para um item vendável ou "RETALHO" para sobras de material.
     */
    @Schema(description = "O tipo de resultado do corte (PRODUTO ou RETALHO).", example = "PRODUTO")
    private String tipo;
}
