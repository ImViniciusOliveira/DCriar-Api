package com.dcriar.api.dto.request.production;

import com.dcriar.api.dto.request.product.DimensoesRequestDTO;
import com.dcriar.api.validation.annotation.ValidOrdemDeCorteRequest;
import com.dcriar.domain.production.enums.ModoCalculo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.*;

/**
 * Data Transfer Object (DTO) para registrar uma nova Ordem de Corte.
 * <p>
 * Este DTO captura todos os dados necessários para registrar a produção de um item,
 * consumindo matéria-prima de um lote e gerando estoque de produto acabado.
 * A validação das regras de negócio (ex: campos obrigatórios por modo de cálculo)
 * é garantida pela anotação customizada {@link ValidOrdemDeCorteRequest}.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidOrdemDeCorteRequest
public class OrdemDeCorteRequestDTO {

    /**
     * O ID do lote de matéria-prima (o 'rolo' ou 'chapa') de onde o material será consumido.
     */
    @Schema(description = "O ID do lote de matéria-prima (o 'rolo') de onde o material será consumido.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long lotePrincipalId;

    /**
     * O ID do produto final (o 'molde') que está sendo fabricado.
     */
    @Schema(description = "O ID do produto final (o 'molde') que está sendo fabricado.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long produtoId;

    /**
     * A quantidade de unidades do produto final que foram fabricadas com sucesso.
     */
    @Schema(description = "A quantidade de unidades do produto final que foram fabricadas com sucesso.", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantidadeProduzida;

    /**
     * O ID do canal de venda para onde o novo estoque de produto acabado será alocado.
     */
    @Schema(description = "O ID do canal de venda para onde o novo estoque de produto acabado será alocado.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long canalVendaDestinoId;

    /**
     * Define o modo de cálculo para o consumo de matéria-prima.
     * <p>
     * <b>AUTOMATICO:</b> O sistema calcula o consumo com base nas dimensões do produto e nas margens fornecidas.
     * <b>MANUAL:</b> O usuário informa o tamanho final exato do corte.
     */
    @Schema(description = "Define o modo de cálculo para o consumo de matéria-prima.", example = "AUTOMÁTICO", requiredMode = Schema.RequiredMode.REQUIRED)
    private ModoCalculo modoCalculo;

    /**
     * As margens (sangria) a serem adicionadas ao corte.
     * <p>
     * Este campo é obrigatório apenas quando o {@code modoCalculo} é 'AUTOMATICO'.
     */
    @Valid
    @Schema(description = "As margens a serem adicionadas ao corte. Obrigatório apenas quando o modoCalculo é 'AUTOMÁTICO'.")
    private MargensRequestDTO margens;

    /**
     * As dimensões finais exatas do corte realizado na matéria-prima.
     * <p>
     * Este campo é obrigatório apenas quando o {@code modoCalculo} é 'MANUAL'.
     */
    @Valid
    @Schema(description = "As dimensões finais exatas do corte. Obrigatório apenas quando o modoCalculo é 'MANUAL'.")
    private DimensoesRequestDTO tamanhoFinal;

    /**
     * Um motivo, observação ou referência para a ordem de produção (ex: número do pedido do cliente).
     */
    @Schema(description = "Um motivo, observação ou referência para a ordem de produção (ex: número do pedido do cliente).", example = "Produção para o pedido #456")
    private String motivo;
}
