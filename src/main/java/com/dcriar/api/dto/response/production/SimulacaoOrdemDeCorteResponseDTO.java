package com.dcriar.api.dto.response.production;

import lombok.Data;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;
import java.util.List;

/**
 * DTO de resposta para a simulação da ordem de corte.
 * Retorna os dados calculados automaticamente, modo de cálculo, margens, lotes disponíveis e links HATEOAS.
 */
@EqualsAndHashCode(callSuper = false)
@Data
@Builder
public class SimulacaoOrdemDeCorteResponseDTO extends RepresentationModel<SimulacaoOrdemDeCorteResponseDTO> {
    private TamanhoFinalResponseDTO tamanhoFinal;
    private String modoCalculo;
    private MargensResponseDTO margens;
    private List<LoteDisponivelResponseDTO> lotesDisponiveis;
}

