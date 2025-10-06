package com.dcriar.api.dto.response.production;

import lombok.Data;
import lombok.Builder;

/**
 * DTO de resposta para margens utilizadas na ordem de corte.
 */
@Data
@Builder
public class MargensResponseDTO {
    private Double superior;
    private Double inferior;
    private Double esquerda;
    private Double direita;
}

