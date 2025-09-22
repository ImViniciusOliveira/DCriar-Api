package com.dcriar.api.dto.request.product;

import com.dcriar.api.validation.annotation.ValidProdutoRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
@ValidProdutoRequest
public class ProdutoRequestDTO {

    @Schema(description = "Nome descritivo do produto.", example = "Etiqueta Adesiva Redonda 5x5cm Kraft")
    private final String nome;

    @Schema(description = "Código único de produto (SKU).", example = "ETQ-KFT-RD5")
    private final String sku;

    @Schema(description = "Descrição detalhada do produto.")
    private final String descricao;

    @Schema(description = "Cor principal do produto.", example = "Marrom")
    private final String cor;

    @Schema(description = "Quantidade de etiquetas por unidade de produto vendido.", example = "100")
    private final Integer unidadesPorProduto;

    @Schema(description = "URL da imagem principal do produto (opcional).")
    private final String fotoPrincipalUrl;

    @Schema(description = "Indica se o produto está ativo para venda.", example = "true")
    private Boolean ativo;

    private final Set<ComposicaoRequestDTO> composicao;

    @Builder
    public record ComposicaoRequestDTO(
            Long materiaPrimaId,
            java.math.BigDecimal gastoMaterialPorUnidade
    ) {}
}
