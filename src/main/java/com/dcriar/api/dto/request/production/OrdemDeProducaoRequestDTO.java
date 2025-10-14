package com.dcriar.api.dto.request.production;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.Set;

/**
 * DTO para requisições de criação/atualização de OrdemDeProducao.
 * Centraliza validações e documentação dos campos necessários.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdemDeProducaoRequestDTO {
    /**
     * ID do produto final a ser fabricado.
     */
    @NotNull
    private Long produtoId;

    /**
     * IDs dos lotes de matéria-prima consumidos.
     */
    @NotNull
    @Size(min = 1)
    private Set<Long> lotesConsumidosIds;

    /**
     * ID do canal de venda destino (opcional).
     */
    private Long canalVendaDestinoId;

    /**
     * Quantidade produzida.
     */
    @NotNull
    @Positive
    private Integer quantidadeProduzida;

    /**
     * Modo de cálculo utilizado.
     */
    @NotNull
    private String modoCalculo;

    /**
     * Margens de segurança (opcional).
     */
    private MargensRequestDTO margens;

    /**
     * Largura final do corte em cm (opcional).
     */
    @Positive
    private BigDecimal larguraFinalCm;

    /**
     * Comprimento final do corte em cm (opcional).
     */
    @Positive
    private BigDecimal comprimentoFinalCm;

    /**
     * Motivo ou observação da ordem de produção.
     */
    @Size(max = 255)
    private String motivo;

    /**
     * Indica se o produto foi rotacionado para otimização do corte.
     */
    private boolean rotacionado;
}

