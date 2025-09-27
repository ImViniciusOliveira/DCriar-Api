package com.dcriar.api.dto.response.production;

import lombok.Builder;
import lombok.Data;
import com.dcriar.api.dto.request.production.MargensRequestDTO;
import java.util.List;

@Data
@Builder
public class OrdemDeCorteResponseDTO {
    private Long id;
    private Long produtoId;
    private Long lotePrincipalId;
    private Long canalVendaDestinoId;
    private Integer quantidadeProduzida;
    private String motivo;
    private String modoCalculo;
    private MargensRequestDTO margens;
    private TamanhoFinalResponseDTO tamanhoFinal;
    private List<CorteRealizadoDTO> cortesRealizados;
}
