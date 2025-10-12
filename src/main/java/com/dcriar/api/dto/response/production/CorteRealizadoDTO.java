package com.dcriar.api.dto.response.production;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO interno para representar um corte (produto ou retalho) gerado durante a produção.
 */
@Data
@Builder
public class CorteRealizadoDTO {
    private BigDecimal larguraCm;
    private BigDecimal comprimentoCm;
    private int quantidade;
    private String tipo;
}
