package com.dcriar.api.dto.request.production;

import com.dcriar.api.dto.request.product.DimensoesRequestDTO;
import com.dcriar.domain.production.enums.ModoCalculo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

/**
 * DTO para receber os dados de uma nova ordem de corte para produção.
 * <p>
 * Esta versão foi refatorada para suportar o modelo de produção híbrido,
 * permitindo que o cálculo do consumo de material seja automático (com margens)
 * ou manual (com dimensões finais fornecidas pelo operador).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdemDeCorteRequestDTO {

    @NotNull(message = "O ID do lote principal é obrigatório.")
    @Schema(description = "O ID do lote principal (o rolo) de onde o material será consumido.", example = "1")
    private Long lotePrincipalId;

    @NotNull(message = "O ID do produto é obrigatório.")
    @Schema(description = "O ID do produto final ('molde') que está a ser fabricado.", example = "1")
    private Long produtoId;

    @NotNull(message = "A quantidade produzida é obrigatória.")
    @Positive(message = "A quantidade produzida deve ser um valor positivo.")
    @Schema(description = "A quantidade de unidades do produto final que foram fabricadas.", example = "100")
    private Integer quantidadeProduzida;

    @NotNull(message = "O ID do canal de venda de destino é obrigatório.")
    @Schema(description = "O ID do canal de venda para onde o novo estoque será alocado.", example = "1")
    private Long canalVendaDestinoId;

    @NotNull(message = "O modo de cálculo é obrigatório.")
    @Schema(description = "Define se o cálculo de consumo é AUTOMATICO ou MANUAL.", example = "AUTOMATICO")
    private ModoCalculo modoCalculo;

    /**
     * As margens a serem adicionadas ao corte.
     * Usado apenas quando o modoCalculo é AUTOMATICO.
     */
    @Valid
    @Schema(description = "As margens a serem adicionadas ao corte (apenas para modo AUTOMATICO).")
    private MargensRequestDTO margens;

    /**
     * As dimensões finais do material consumido, informadas pelo operador.
     * Usado apenas quando o modoCalculo é MANUAL.
     */
    @Valid
    @Schema(description = "As dimensões finais do corte (apenas para modo MANUAL).")
    private DimensoesRequestDTO tamanhoFinal;

    @Schema(description = "Um motivo ou observação para a ordem de produção.", example = "Produção para o pedido #456")
    private String motivo;
}