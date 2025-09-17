package com.dcriar.api.dto.response.stock;

import com.dcriar.domain.stock.entity.enuns.UnidadeDeMedida;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para enviar dados de um Tipo de Matéria-Prima como resposta da API.
 *
 * @param id O ID único gerado para o tipo de matéria-prima.
 * @param nome O nome do tipo de matéria-prima.
 * @param unidadeDeConsumo A unidade em que o material é consumido.
 */
@Schema(description = "Representação de um tipo de matéria-prima no sistema.")
public record TipoMateriaPrimaResponseDTO(
        @Schema(description = "ID único do tipo de matéria-prima.", example = "1")
        Long id,

        @Schema(description = "Nome do tipo de matéria-prima.", example = "Adesivo Vinil Branco Brilho")
        String nome,

        @Schema(description = "Unidade em que o material é consumido.", example = "CENTIMETRO_QUADRADO")
        UnidadeDeMedida unidadeDeConsumo
) {
}

