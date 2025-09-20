package com.dcriar.api.dto.response.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * DTO (Data Transfer Object) para enviar dados de um Produto como resposta da API.
 * <p>
 * Fornece uma representação segura e formatada da entidade para o cliente,
 * agora enriquecida com um resumo completo do seu estado de estoque.
 */
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

    @Schema(description = "Descrição detalhada do produto.")
    private String descricao;

    @Schema(description = "Cor principal do produto.", example = "Marrom")
    private String cor;

    @Schema(description = "Quantidade de etiquetas em um único produto vendido.", example = "100")
    private Integer unidadesPorProduto;

    @Schema(description = "Indica se o produto está ativo para venda.", example = "true")
    private boolean ativo;

    @Schema(description = "URL da imagem principal do produto.")
    private String fotoPrincipalUrl;

    @Schema(description = "O total de unidades físicas deste produto no estoque (Estoque Mestre).")
    private Integer estoqueFisicoTotal;

    @Schema(description = "O total de unidades já distribuídas pelos canais de venda.")
    private Integer estoqueDistribuidoTotal;

    @Schema(description = "O saldo de unidades disponíveis para serem alocadas a um canal.")
    private Integer estoqueDisponivelParaAlocar;
}
