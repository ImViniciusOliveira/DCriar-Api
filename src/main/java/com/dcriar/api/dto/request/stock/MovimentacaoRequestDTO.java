package com.dcriar.api.dto.request.stock;

import com.dcriar.api.validation.annotation.ValidMovimentacaoRequest;
import com.dcriar.domain.stock.entity.enuns.TipoMovimentacao;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@ValidMovimentacaoRequest
public class MovimentacaoRequestDTO {

    @Schema(description = "O tipo da movimentação a ser registada.", example = "SAIDA_PRODUCAO")
    private TipoMovimentacao tipo;

    @Schema(description = "A quantidade a ser movimentada. Use valor negativo para saídas.", example = "-1.50")
    private BigDecimal quantidade;

    @Schema(description = "Um motivo ou observação para a movimentação.", example = "Uso na produção do pedido #123")
    private String motivo;
}
