package com.dcriar.api.dto.response.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoResponseDTO {

    @Schema(description = "ID único do produto.", example = "1")
    private Long id;

    @Schema(description = "Nome do produto.", example = "Etiqueta Adesiva Redonda 5x5cm Kraft")
    private String nome;

    @Schema(description = "Código único de produto (SKU).", example = "ETQ-KRAFT-RD5")
    private String sku;

    @Schema(description = "Descrição detalhada do produto.", example = "Etiqueta adesiva redonda de papel kraft 5x5cm")
    private String descricao;

    @Schema(description = "Cor principal do produto.", example = "Marrom")
    private String cor;

    @Schema(description = "Quantidade de unidades por produto vendido.", example = "100")
    private Integer unidadesPorProduto;

    @Schema(description = "Indica se o produto está ativo para venda.", example = "true")
    private boolean ativo;

    @Schema(description = "URL da imagem principal do produto.")
    private String fotoPrincipalUrl;

    @Schema(description = "Quantidade total em estoque (Estoque Mestre).", example = "150")
    private Integer estoqueFisicoTotal;

    @Schema(description = "Quantidade distribuída pelos canais de venda.", example = "100")
    private Integer estoqueDistribuidoTotal;

    @Schema(description = "Saldo de unidades disponíveis para alocação em canais de venda.", example = "50")
    private Integer estoqueDisponivelParaAlocar;

    @Schema(description = "Composição do produto em matérias-primas associadas.")
    private Set<ComposicaoResponseDTO> composicao;
}
