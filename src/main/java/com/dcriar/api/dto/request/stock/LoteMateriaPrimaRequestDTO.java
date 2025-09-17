package com.dcriar.api.dto.request.stock;

import com.dcriar.domain.stock.entity.enuns.UnidadeDeMedida;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO (Data Transfer Object) para receber os dados de criação de um novo Lote de Matéria-Prima.
 * <p>
 * Este DTO encapsula todas as informações necessárias para dar entrada de um novo
 * item físico no estoque, incluindo seu tipo, quantidade inicial e atributos específicos.
 */
@Getter
@Setter
@Builder
public class LoteMateriaPrimaRequestDTO {

    @NotNull(message = "O ID do tipo de matéria-prima é obrigatório.")
    @Schema(description = "ID do Tipo de Matéria-Prima ao qual este lote pertence.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long tipoMateriaPrimaId;

    @NotNull(message = "A unidade de estoque é obrigatória.")
    @Schema(description = "Unidade de medida em que este lote é armazenado fisicamente.", example = "METRO_LINEAR", requiredMode = Schema.RequiredMode.REQUIRED)
    private UnidadeDeMedida unidadeDeEstoque;

    @NotNull(message = "A quantidade inicial é obrigatória.")
    @Positive(message = "A quantidade inicial deve ser maior que zero.")
    @Schema(description = "A quantidade inicial de material que está a dar entrada no estoque com este lote.", example = "50.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal quantidadeInicial;

    @Schema(description = "Atributos flexíveis do lote, como largura, fornecedor, etc.",
            example = "{\"larguraMm\": 500, \"fornecedor\": \"Adesivos Sul\"}")
    private Map<String, Object> atributos;

    @Schema(description = "Motivo opcional para a movimentação de entrada.", example = "Compra conforme Nota Fiscal #12345")
    private String motivo;
}
