package com.dcriar.api.dto.request.production;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO para a criação de uma nova Ordem de Produção do tipo CONSUMO DIRETO.
 */
@Data
@Builder
public class OrdemDeConsumoDiretoRequestDTO {

    @NotNull
    @Schema(description = "ID do produto a ser fabricado (deve ser um produto de consumo direto, como líquidos, pós ou unidades).", example = "5")
    private Long produtoId;

    @NotEmpty
    @Schema(description = "Lista de IDs dos lotes de matéria-prima a serem consumidos.", example = "[4]")
    private List<Long> lotesConsumidosIds;

    @Schema(description = "ID do canal de venda de destino do estoque (opcional).", example = "1")
    private Long canalVendaDestinoId;

    @NotNull
    @Positive
    @Schema(description = "Quantidade de unidades do produto a serem produzidas.", example = "250")
    private Integer quantidadeProduzida;

    @Schema(description = "Motivo ou referência para a ordem.", example = "Reposição de Estoque Interno")
    private String motivo;
}
