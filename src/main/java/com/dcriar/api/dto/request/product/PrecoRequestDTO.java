package com.dcriar.api.dto.request.product;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

/**
 * DTO para entrada de dados de preço de produto.
 * <p>
 * Utilizado para criação e atualização de preços, centralizando validações e estrutura de dados.
 * Todos os campos devem ser validados conforme regras de negócio e documentados para uso na API.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrecoRequestDTO {
    /**
     * ID do produto ao qual o preço está associado.
     * <p>Obrigatório para vincular o preço ao produto correto.</p>
     */
    @NotNull
    private Long produtoId;

    /**
     * Tipo de preço (ex: VAREJO, ATACADO).
     * <p>Deve corresponder ao enum TipoPreco.</p>
     */
    @NotNull
    private String tipoPreco;

    /**
     * Valor base do preço do produto.
     * <p>Deve ser maior ou igual a zero.</p>
     */
    @NotNull
    @DecimalMin("0.0")
    private BigDecimal valor;

    /**
     * Valor promocional do produto, se houver promoção ativa.
     * <p>Deve ser maior ou igual a zero.</p>
     */
    @DecimalMin("0.0")
    private BigDecimal valorPromocional;

    /**
     * Indica se a promoção está ativa.
     * <p>Obrigatório para definir se o valor promocional será aplicado.</p>
     */
    @NotNull
    private Boolean promocaoAtiva;
}
