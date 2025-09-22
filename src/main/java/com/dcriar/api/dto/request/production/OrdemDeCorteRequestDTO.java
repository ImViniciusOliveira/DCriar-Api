package com.dcriar.api.dto.request.production;

import com.dcriar.api.validation.annotation.ValidOrdemDeCorteRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidOrdemDeCorteRequest
public class OrdemDeCorteRequestDTO {

    @Schema(description = "O ID do lote principal (o rolo) de onde o material será consumido.", example = "1")
    private Long lotePrincipalId;

    @Schema(description = "O comprimento, em centímetros, que será cortado do rolo principal.", example = "100.0")
    private BigDecimal comprimentoDeCorteCm;

    @Schema(description = "A largura, em centímetros, que será utilizada para o produto.", example = "40.0")
    private BigDecimal larguraDeCorteCm;

    @Schema(description = "Um motivo ou observação para a ordem de produção.", example = "Produção para o pedido #456")
    private String motivo;

    @Schema(description = "O ID do produto final que está sendo fabricado com este corte.", example = "1")
    private Long produtoId;

    @Schema(description = "A quantidade de unidades do produto final que foram fabricadas.", example = "100")
    private Integer quantidadeProduzida;

    @Schema(description = "O ID do canal de venda para onde o novo estoque será alocado.", example = "1")
    private Long canalVendaDestinoId;
}
