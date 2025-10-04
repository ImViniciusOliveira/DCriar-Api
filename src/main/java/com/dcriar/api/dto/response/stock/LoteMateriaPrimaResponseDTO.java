package com.dcriar.api.dto.response.stock;

import com.dcriar.domain.stock.entity.enums.UnidadeDeMedida;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Data Transfer Object (DTO) que representa a resposta de um Lote de Matéria-Prima.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoteMateriaPrimaResponseDTO {

    @Schema(description = "ID único do lote.", example = "1")
    private Long id;

    @Schema(description = "ID do tipo de matéria-prima a que este lote pertence.", example = "10")
    private Long tipoMateriaPrimaId;

    @Schema(description = "Nome do tipo de matéria-prima a que este lote pertence.", example = "Adesivo Kraft Pardo")
    private String nomeTipoMateriaPrima;

    @Schema(description = "Unidade em que o saldo deste lote é medido.", example = "METRO_LINEAR")
    private UnidadeDeMedida unidadeDeEstoque;

    @Schema(description = "O saldo de estoque atual deste lote.", example = "49.00")
    private BigDecimal saldoEstoque;

    @Schema(description = "Atributos flexíveis que descrevem as especificações deste lote físico.")
    private Map<String, Object> atributos;

    @Schema(description = "ID do lote que deu origem a este (se for um retalho).", example = "1")
    private Long loteDeOrigemId;
}
