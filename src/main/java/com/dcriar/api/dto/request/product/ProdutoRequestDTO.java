package com.dcriar.api.dto.request.product;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Set;

/**
 * DTO (Data Transfer Object) para receber os dados de criação ou atualização de um Produto.
 * <p>
 * Esta classe agrega todas as informações que o cliente da API precisa enviar
 * para registrar um novo produto ou modificar um existente. Utiliza anotações do
 * Jakarta Bean Validation para garantir a integridade dos dados na camada de entrada.
 */
@Getter
@Builder
public class ProdutoRequestDTO {

        @Schema(description = "Nome descritivo do produto.", example = "Etiqueta Adesiva Redonda 5x5cm Kraft", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O nome do produto é obrigatório.")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres.")
        private final String nome;

        @Schema(description = "Código único de produto (Stock Keeping Unit).", example = "ETQ-KFT-RD5", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O SKU do produto é obrigatório.")
        @Size(max = 50, message = "O SKU deve ter no máximo 50 caracteres.")
        private final String sku;

        @Schema(description = "Descrição detalhada do produto (opcional).", example = "Etiqueta em papel kraft ideal para personalização de embalagens.")
        private final String descricao;

        @Schema(description = "Cor principal do produto.", example = "Marrom", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "A cor do produto é obrigatória.")
        @Size(max = 50, message = "A cor deve ter no máximo 50 caracteres.")
        private final String cor;

        @Schema(description = "Quantidade de etiquetas por unidade de produto vendido.", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "A quantidade de unidades por produto é obrigatória.")
        @Positive(message = "A quantidade de unidades deve ser um número positivo.")
        private final Integer unidadesPorProduto;

        @Schema(description = "URL da imagem principal do produto (opcional).", example = "https://servidor.com/imagens/etiqueta_kraft.jpg")
        private final String fotoPrincipalUrl;

        @Schema(description = "Indica se o produto está ativo para venda.", example = "true")
        private Boolean ativo;

        @ArraySchema(schema = @Schema(description = "Lista dos materiais que compõem o produto e a quantidade gasta de cada um."))
        @Valid
        private final Set<ComposicaoRequestDTO> composicao;

        /**
         * DTO aninhado para representar um item da "receita" de um produto.
         */
        @Getter
        @Builder
        public static class ComposicaoRequestDTO {

                @Schema(description = "ID da Matéria-Prima utilizada.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "O ID da matéria-prima é obrigatório.")
                private final Long materiaPrimaId;

                @Schema(description = "Área gasta do material para produzir uma unidade do produto, em centímetros quadrados (cm²).", example = "25.5", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "O gasto de material é obrigatório.")
                @Positive(message = "O gasto de material deve ser um número positivo.")
                private final BigDecimal gastoMaterialPorUnidade;
        }
}

