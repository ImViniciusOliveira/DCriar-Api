package com.dcriar.api.dto.response.production;

import lombok.Data;
import lombok.Builder;
import java.util.Map;

/**
 * DTO de resposta para lotes disponíveis na ordem de corte.
 */
@Data
@Builder
public class LoteDisponivelResponseDTO {
    private Long id;
    private Long tipoMateriaPrimaId;
    private String nomeTipoMateriaPrima;
    private String unidadeDeEstoque;
    private Double saldoEstoque;
    private Map<String, Object> atributos;
    private Long loteDeOrigemId;
}

