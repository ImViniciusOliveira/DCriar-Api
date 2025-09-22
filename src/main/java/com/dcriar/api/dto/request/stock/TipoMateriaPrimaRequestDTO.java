package com.dcriar.api.dto.request.stock;

import com.dcriar.api.validation.annotation.ValidTipoMateriaPrimaRequest;
import com.dcriar.domain.stock.entity.enuns.UnidadeDeMedida;
import io.swagger.v3.oas.annotations.media.Schema;

@ValidTipoMateriaPrimaRequest
public record TipoMateriaPrimaRequestDTO(
        @Schema(description = "Nome único do tipo de matéria-prima.", example = "Adesivo Vinil Branco Brilho")
        String nome,

        @Schema(description = "Unidade em que a 'receita' consome este material.", example = "CENTIMETRO_QUADRADO")
        UnidadeDeMedida unidadeDeConsumo
) {
}
