package com.dcriar.api.dto.request.production;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO para receber os dados de uma nova ordem de corte para produção.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdemDeCorteRequestDTO {

    @NotNull(message = "O ID do lote principal é obrigatório.")
    @Schema(description = "O ID do lote principal (o rolo) de onde o material será consumido.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long lotePrincipalId;

    @NotNull(message = "O comprimento de corte é obrigatório.")
    @Positive(message = "O comprimento de corte deve ser um valor positivo.")
    @Schema(description = "O comprimento, em centímetros, que será cortado do rolo principal.", example = "100.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal comprimentoDeCorteCm;

    @NotNull(message = "A largura de corte é obrigatória.")
    @Positive(message = "A largura de corte deve ser um valor positivo.")
    @Schema(description = "A largura, em centímetros, que será utilizada para o produto.", example = "40.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal larguraDeCorteCm;

    @Schema(description = "Um motivo ou observação para a ordem de produção (ex: Pedido do cliente #456).", example = "Produção para o pedido #456")
    private String motivo;

    /**
     * O ID do produto final que está a ser fabricado com este corte.
     */
    @NotNull(message = "O ID do produto fabricado é obrigatório.")
    @Schema(description = "O ID do produto final que está a ser fabricado com este corte.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long produtoId;

    /**
     * A quantidade de unidades do produto final que foram fabricadas nesta ordem.
     */
    @NotNull(message = "A quantidade produzida é obrigatória.")
    @Positive(message = "A quantidade produzida deve ser um valor positivo.")
    @Schema(description = "A quantidade de unidades do produto final que foram fabricadas.", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantidadeProduzida;

    /**
     * O ID do canal de venda para onde o novo estoque de produto acabado deve ser alocado.
     */
    @NotNull(message = "O ID do canal de venda de destino é obrigatório.")
    @Schema(description = "O ID do canal de venda para onde o novo estoque será alocado.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long canalVendaDestinoId;
}

