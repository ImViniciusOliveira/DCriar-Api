package com.dcriar.domain.production.service;

import com.dcriar.api.dto.request.production.OrdemDeCorteRequestDTO;

/**
 * Interface que define o contrato para a lógica de negócio de Ordens de Produção.
 * <p>
 * Abstrai os processos de fabrico, como o corte de materiais e a gestão
 * automatizada de sobras (retalhos).
 */
public interface OrdemDeProducaoService {

    /**
     * Processa uma ordem de corte, consumindo material de um lote principal
     * e criando automaticamente um novo lote para a sobra (retalho), se aplicável.
     *
     * @param requestDTO O DTO com os detalhes da ordem de corte.
     */
    void processarOrdemDeCorte(OrdemDeCorteRequestDTO requestDTO);
}

