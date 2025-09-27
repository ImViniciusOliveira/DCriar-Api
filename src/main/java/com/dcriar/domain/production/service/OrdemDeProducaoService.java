package com.dcriar.domain.production.service;

import com.dcriar.api.dto.request.production.OrdemDeCorteRequestDTO;
import com.dcriar.api.dto.response.production.OrdemDeCorteResponseDTO;

/**
 * Interface que define o contrato para a lógica de negócio de Ordens de Produção.
 * <p>
 * Abstrai os processos de fabrico, como o corte de materiais e a gestão
 * automatizada de sobras (retalhos).
 */
public interface OrdemDeProducaoService {

    /**
     * Processa uma ordem de corte, consumindo material de um lote principal,
     * criando um novo lote para a sobra (retalho), se aplicável, e dando
     * entrada do produto acabado no estoque mestre e no estoque do canal de destino.
     *
     * @param requestDTO O DTO com os detalhes completos da ordem de corte.
     * @return OrdemDeCorteResponseDTO com os dados necessários para o front.
     */
    OrdemDeCorteResponseDTO processarOrdemDeCorte(OrdemDeCorteRequestDTO requestDTO);

    /**
     * Exclui uma ordem de corte pelo id.
     * @param id O id da ordem de corte a ser excluída.
     */
    void excluirOrdemDeCorte(Long id);
}