package com.dcriar.api.dto.request.stock;

import com.dcriar.api.validation.annotation.ValidLoteMateriaPrimaRequest;
import com.dcriar.domain.stock.entity.enuns.UnidadeDeMedida;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@Builder
@ValidLoteMateriaPrimaRequest
public class LoteMateriaPrimaRequestDTO {

    @Schema(description = "ID do Tipo de Matéria-Prima ao qual este lote pertence.", example = "1")
    private Long tipoMateriaPrimaId;

    @Schema(description = "Unidade de medida em que este lote é armazenado fisicamente.", example = "METRO_LINEAR")
    private UnidadeDeMedida unidadeDeEstoque;

    @Schema(description = "A quantidade inicial de material que está a dar entrada no estoque.", example = "50.00")
    private BigDecimal quantidadeInicial;

    @Schema(description = "O valor total pago por este lote.", example = "150.00")
    private BigDecimal custoTotalLote;

    @Schema(description = "Atributos flexíveis do lote, como largura, fornecedor, etc.")
    private Map<String, Object> atributos;

    @Schema(description = "Motivo opcional para a movimentação de entrada.")
    private String motivo;
}
