package com.dcriar.api.dto.request.production;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

/**
 * DTO para entrada de dados de CorteRealizado.
 * <p>
 * Utilizado para criação e atualização de cortes realizados em uma ordem de produção.
 * Todos os campos possuem validação e documentação para uso seguro na API.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorteRealizadoRequestDTO {
    /**
     * ID da ordem de produção à qual o corte está associado.
     */
    @NotNull
    private Long ordemDeProducaoId;

    /**
     * Largura do corte em centímetros.
     */
    @NotNull
    @DecimalMin("0.0")
    private BigDecimal larguraCm;

    /**
     * Comprimento do corte em centímetros.
     */
    @NotNull
    @DecimalMin("0.0")
    private BigDecimal comprimentoCm;

    /**
     * Quantidade de peças produzidas com este corte.
     */
    @NotNull
    @Min(1)
    private Integer quantidade;

    /**
     * Tipo de resultado do corte ("PRODUTO" ou "RETALHO").
     */
    @NotBlank
    private String tipo;
}