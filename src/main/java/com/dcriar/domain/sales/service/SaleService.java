package com.dcriar.domain.sales.service;

import com.dcriar.api.dto.request.sales.SaleRequestDTO;
import com.dcriar.api.dto.response.sales.SaleResponseDTO;

import java.util.List;

/**
 * Interface que define o contrato para a lógica de negócio de Vendas (Sales).
 */
public interface SaleService {

    /**
     * Regista uma nova venda no sistema e orquestra a baixa automática de estoque.
     */
    SaleResponseDTO registerSale(SaleRequestDTO requestDTO);

    /**
     * Lista todas as vendas registadas no sistema.
     *
     * @return Uma lista com os DTOs de resposta de todas as vendas.
     */
    List<SaleResponseDTO> findAll();

    /**
     * Busca uma venda específica pelo seu ID.
     *
     * @param id O ID da venda a ser buscada.
     * @return O DTO de resposta da venda encontrada.
     */
    SaleResponseDTO findById(Long id);
}

