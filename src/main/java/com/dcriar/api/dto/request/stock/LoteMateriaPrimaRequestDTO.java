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
 */
@Getter
@Setter
@Builder
public class LoteMateriaPrimaRequestDTO {

    @NotNull(message = "O ID do tipo de matéria-prima é obrigatório.")
    @Schema(description = "ID do Tipo de Matéria-Prima ao qual este lote pertence.", example = "1")
    private Long tipoMateriaPrimaId;

    @NotNull(message = "A unidade de estoque é obrigatória.")
    @Schema(description = "Unidade de medida em que este lote é armazenado fisicamente.", example = "METRO_LINEAR")
    private UnidadeDeMedida unidadeDeEstoque;

    @NotNull(message = "A quantidade inicial é obrigatória.")
    @Positive(message = "A quantidade inicial deve ser maior que zero.")
    @Schema(description = "A quantidade inicial de material que está a dar entrada no estoque.", example = "50.00")
    private BigDecimal quantidadeInicial;

    /**
     * O custo total pago por este lote.
     */
    @NotNull(message = "O custo total do lote é obrigatório.")
    @Positive(message = "O custo total deve ser maior que zero.")
    @Schema(description = "O valor total pago por este lote.", example = "150.00")
    private BigDecimal custoTotalLote;

    @Schema(description = "Atributos flexíveis do lote, como largura, fornecedor, etc.")
    private Map<String, Object> atributos;

    @Schema(description = "Motivo opcional para a movimentação de entrada.")
    private String motivo;
}

