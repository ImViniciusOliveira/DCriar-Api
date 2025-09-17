package com.dcriar.api.dto.response.product;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO (Data Transfer Object) para enviar dados de um Produto como resposta da API.
 * <p>
 * Fornece uma representação segura e formatada da entidade para o cliente.
 */
@Schema(description = "Representação de um produto no sistema.")
public record ProdutoResponseDTO(
        @Schema(description = "ID único do produto.", example = "1")
        Long id,

        @Schema(description = "Nome do produto.", example = "Etiqueta Adesiva Redonda 5x5cm Kraft")
        String nome,

        @Schema(description = "Código único de produto (SKU).", example = "ETQ-KRAFT-RD5")
        String sku,

        @Schema(description = "Descrição detalhada do produto.", example = "Etiquetas em papel kraft para personalização de embalagens.")
        String descricao,

        @Schema(description = "Cor principal do produto.", example = "Marrom")
        String cor,

        @Schema(description = "Quantidade de etiquetas em um único produto vendido.", example = "100")
        Integer unidadesPorProduto,

        @Schema(description = "Indica se o produto está ativo para venda.", example = "true")
        boolean ativo,

        @Schema(description = "URL da imagem principal do produto.", example = "https://server.com/image.png")
        String fotoPrincipalUrl
) {
}
