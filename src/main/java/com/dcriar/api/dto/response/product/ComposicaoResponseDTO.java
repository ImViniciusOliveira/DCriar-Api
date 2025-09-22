package com.dcriar.api.dto.response.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComposicaoResponseDTO {

    @Schema(description = "ID da matéria-prima utilizada.", example = "1")
    private Long materiaPrimaId;

    @Schema(description = "Nome da matéria-prima utilizada.", example = "Adesivo Vinil Branco Brilho")
    private String nomeMateriaPrima;

    @Schema(description = "Quantidade da matéria-prima consumida por unidade do produto.", example = "25.00")
    private BigDecimal gastoMaterialPorUnidade;
}
