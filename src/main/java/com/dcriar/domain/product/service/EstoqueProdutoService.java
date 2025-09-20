package com.dcriar.domain.product.service;

import com.dcriar.api.dto.request.product.AjusteEstoqueProdutoRequestDTO;
import com.dcriar.api.dto.request.product.AjusteEstoqueRequestDTO;
import com.dcriar.api.dto.response.product.EstoqueResponseDTO;
import com.dcriar.api.dto.response.product.MovimentacaoProdutoResponseDTO;

import java.util.List;

/**
 * Interface que define o contrato para a lógica de negócio de Estoque de Produtos Acabados.
 */
public interface EstoqueProdutoService {

    /**
     * Ajusta o estoque de um produto acabado em um canal de venda específico (distribuição).
     */
    EstoqueResponseDTO ajustarEstoque(AjusteEstoqueRequestDTO requestDTO);

    /**
     * Ajusta o Estoque Físico Total de um produto ("Estoque Mestre").
     */
    void ajustarEstoqueFisico(AjusteEstoqueProdutoRequestDTO requestDTO);

    /**
     * Consulta o estoque de um produto específico em um determinado canal de venda.
     */
    EstoqueResponseDTO consultarEstoque(Long produtoId, Long canalVendaId);

    /**
     * Lista todos os registros de estoque para um determinado produto, em todos os canais de venda.
     */
    List<EstoqueResponseDTO> listarEstoquesPorProduto(Long produtoId);

    /**
     * Lista todo o histórico de movimentações ("Livro-Razão") do Estoque Físico Total de um produto.
     *
     * @param produtoId O ID do produto cujo histórico será consultado.
     * @return Uma lista de DTOs, cada um representando uma movimentação.
     */
    List<MovimentacaoProdutoResponseDTO> listarMovimentacoesPorProduto(Long produtoId);
}

