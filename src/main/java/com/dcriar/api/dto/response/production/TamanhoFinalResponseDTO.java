package com.dcriar.api.dto.response.production;

import lombok.Data;
import lombok.Builder;

/**
 * DTO de resposta para o tamanho final calculado na ordem de corte.
 */
@Data
@Builder
public class TamanhoFinalResponseDTO {
    private Double largura;
    private Double comprimento;
}
