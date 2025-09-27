package com.dcriar.api.dto.response.production;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TamanhoFinalResponseDTO {
    private Double larguraCm;
    private Double comprimentoCm;
}

