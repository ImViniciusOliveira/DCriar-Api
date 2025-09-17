package com.dcriar.api.dto.request.stock;

import com.dcriar.domain.stock.entity.enuns.UnidadeDeMedida;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


/**
 * DTO para criar ou atualizar um Tipo de Matéria-Prima.
 *
 * @param nome O nome único do tipo de matéria-prima.
 * @param unidadeDeConsumo A unidade em que a "receita" de um produto consome este material.
 */
public record TipoMateriaPrimaRequestDTO(
        @NotBlank
        @Size(max = 150)
        @Schema(description = "Nome único do tipo de matéria-prima.", example = "Adesivo Vinil Branco Brilho", requiredMode = Schema.RequiredMode.REQUIRED)
        String nome,

        @NotNull
        @Schema(description = "Unidade em que a 'receita' consome este material.", example = "CENTIMETRO_QUADRADO", requiredMode = Schema.RequiredMode.REQUIRED)
        UnidadeDeMedida unidadeDeConsumo
) {
}

