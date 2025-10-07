package com.dcriar.api.dto.response.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Data Transfer Object) que representa o estoque de um produto em um canal de venda específico.
 * É um componente do {@link ProdutoEstoqueDTO}, detalhando a quantidade de um produto
 * disponível em um determinado canal.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CanalEstoqueDTO {

    /**
     * O nome do canal de venda (ex: "LOJA_FISICA", "MERCADO_LIVRE").
     */
    @Schema(description = "O nome do canal de venda.", example = "MERCADO_LIVRE")
    private String canalNome;

    /**
     * A quantidade de unidades do produto disponíveis neste canal.
     */
    @Schema(description = "A quantidade de unidades do produto disponíveis neste canal.", example = "25")
    private int quantidade;
}
