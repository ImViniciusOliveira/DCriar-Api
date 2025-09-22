package com.dcriar.api.dto.request.product;

import com.dcriar.api.validation.annotation.ValidComposicaoRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidComposicaoRequest
public class ComposicaoRequestDTO {

    @Schema(description = "ID da matéria-prima usada no produto.", example = "2")
    private Long materiaPrimaId;

    @Schema(description = "Quantidade de matéria-prima usada por unidade do produto.", example = "0.5")
    private BigDecimal gastoMaterialPorUnidade;

}
