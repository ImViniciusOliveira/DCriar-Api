package com.dcriar.api.dto.request.product;

import com.dcriar.api.validation.annotation.ValidProdutoRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * DTO para receber os dados de criação ou atualização de um Produto ("molde").
 * <p>
 * Esta versão foi refatorada para alinhar-se com a nova lógica de "molde",
 * recebendo as dimensões unitárias e o tipo de matéria-prima principal.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidProdutoRequest
public class ProdutoRequestDTO {

    @Schema(description = "Nome descritivo do produto.", example = "Etiqueta Adesiva Redonda 5x5cm Kraft", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nome;

    @Schema(description = "Código único de produto (SKU).", example = "ETQ-KFT-RD5", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sku;

    @Schema(description = "Descrição detalhada do produto.")
    private String descricao;

    @Schema(description = "Cor principal do produto.", example = "Marrom", requiredMode = Schema.RequiredMode.REQUIRED)
    private String cor;

    @Schema(description = "Quantidade de itens por unidade de produto vendido.", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer unidadesPorProduto;

    @Schema(description = "URL da imagem principal do produto (opcional).")
    private String fotoPrincipalUrl;

    @Schema(description = "Indica se o produto está ativo para venda.", example = "true")
    private Boolean ativo;

    @Schema(description = "ID do Tipo de Matéria-Prima principal que este produto consome.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long tipoMateriaPrimaId;

    @Schema(description = "As dimensões de uma única unidade do produto.", requiredMode = Schema.RequiredMode.REQUIRED)
    private DimensoesRequestDTO dimensoesUnitarias;
}

