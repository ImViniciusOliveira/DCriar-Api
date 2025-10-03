package com.dcriar.api.dto.response.stock;

import com.dcriar.domain.stock.entity.enums.UnidadeDeMedida;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Data Transfer Object (DTO) que representa a resposta de um Lote de Matéria-Prima.
 * <p>
 * Este DTO fornece uma visão detalhada de um lote específico, incluindo seu tipo,
 * saldo em estoque, atributos e sua origem (se aplicável).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoteMateriaPrimaResponseDTO {

    /**
     * O ID único do lote de matéria-prima.
     */
    @Schema(description = "ID único do lote.", example = "1")
    private Long id;

    /**
     * O nome do tipo de matéria-prima a que este lote pertence.
     */
    @Schema(description = "Nome do tipo de matéria-prima a que este lote pertence.", example = "Adesivo Kraft Pardo")
    private String nomeTipoMateriaPrima;

    /**
     * A unidade de medida em que o saldo deste lote é controlado.
     */
    @Schema(description = "Unidade em que o saldo deste lote é medido.", example = "METRO_LINEAR")
    private UnidadeDeMedida unidadeDeEstoque;

    /**
     * O saldo de estoque atual deste lote, calculado a partir de todas as suas movimentações.
     */
    @Schema(description = "O saldo de estoque atual deste lote, calculado a partir de todas as suas movimentações.", example = "49.00")
    private BigDecimal saldoEstoque;

    /**
     * Atributos flexíveis (chave-valor) que descrevem as especificações deste lote físico,
     * como largura, gramatura, etc.
     */
    @Schema(description = "Atributos flexíveis que descrevem as especificações deste lote físico.")
    private Map<String, Object> atributos;

    /**
     * O ID do lote que deu origem a este lote.
     * <p>
     * Este campo é preenchido quando o lote é um retalho ou sobra de outro processo de corte.
     * Será nulo se for um lote principal (por exemplo, originado de uma compra).
     */
    @Schema(description = "ID do lote que deu origem a este (se for um retalho). Será nulo para lotes principais.", example = "1")
    private Long loteDeOrigemId;
}
