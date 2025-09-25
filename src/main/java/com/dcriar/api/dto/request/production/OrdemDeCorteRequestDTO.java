package com.dcriar.api.dto.request.production;

import com.dcriar.api.dto.request.product.DimensoesRequestDTO;
import com.dcriar.api.validation.annotation.ValidOrdemDeCorteRequest;
import com.dcriar.domain.production.enums.ModoCalculo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidOrdemDeCorteRequest
public class OrdemDeCorteRequestDTO {

    @Schema(description = "O ID do lote principal (o rolo) de onde o material será consumido.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long lotePrincipalId;

    @Schema(description = "O ID do produto final ('molde') que está a ser fabricado.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long produtoId;

    @Schema(description = "A quantidade de unidades do produto final que foram fabricadas.", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantidadeProduzida;

    @Schema(description = "O ID do canal de venda para onde o novo estoque será alocado.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long canalVendaDestinoId;

    @Schema(description = "Define se o cálculo de consumo é AUTOMATICO ou MANUAL.", example = "AUTOMATICO", requiredMode = Schema.RequiredMode.REQUIRED)
    private ModoCalculo modoCalculo;

    @Schema(description = "As margens a serem adicionadas ao corte (apenas para modo AUTOMATICO).")
    private MargensRequestDTO margens;

    @Schema(description = "As dimensões finais do corte (apenas para modo MANUAL).")
    private DimensoesRequestDTO tamanhoFinal;

    @Schema(description = "Um motivo ou observação para a ordem de produção.", example = "Produção para o pedido #456")
    private String motivo;
}