package com.dcriar.api.dto.response.production;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorteRealizadoDTO {
    private BigDecimal larguraCm;
    private BigDecimal comprimentoCm;
    private int quantidade; // quantas faixas desse tipo
    private String tipo; // "PRODUTO" ou "RETALHO"
}

