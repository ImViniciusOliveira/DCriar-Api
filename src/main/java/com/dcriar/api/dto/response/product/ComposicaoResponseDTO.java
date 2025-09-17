package com.dcriar.api.dto.response.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO que representa um item da "receita" de um produto na resposta da API.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComposicaoResponseDTO {

    @Schema(description = "ID do tipo de matéria-prima utilizada.", example = "1")
    private Long materiaPrimaId;

    @Schema(description = "Nome do tipo de matéria-prima.", example = "Adesivo Vinil Branco Brilho")
    private String nomeMateriaPrima;

    /**
     * CORREÇÃO: O nome do campo foi alterado para ser genérico,
     * alinhando-se com a entidade ComposicaoProduto.
     */
    @Schema(description = "Quantidade do material gasta por unidade de produto.", example = "25.00")
    private BigDecimal gastoMaterialPorUnidade;
}

