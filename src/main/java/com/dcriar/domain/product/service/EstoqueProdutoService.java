package com.dcriar.domain.product.service;

import com.dcriar.api.dto.request.product.AjusteEstoqueRequestDTO;
import com.dcriar.api.dto.response.product.EstoqueResponseDTO;
import java.util.List;

/**
 * Interface que define o contrato para a lógica de negócio de Estoque de Produtos Acabados.
 * <p>
 * Abstrai as operações de gerenciamento do estoque de produtos finalizados,
 * como ajustes manuais e consultas.
 */
public interface EstoqueProdutoService {

    /**
     * Ajusta o estoque de um produto acabado em um canal de venda específico.
     * <p>
     * Esta operação pode adicionar (quantidade positiva) ou remover (quantidade negativa)
     * itens do estoque. Se não houver um registro de estoque para a combinação de
     * produto e canal, um novo será criado.
     *
     * @param requestDTO O DTO com os detalhes do ajuste.
     * @return O DTO de resposta com o estado atualizado do estoque.
     */
    EstoqueResponseDTO ajustarEstoque(AjusteEstoqueRequestDTO requestDTO);

    /**
     * Consulta o estoque de um produto específico em um determinado canal de venda.
     *
     * @param produtoId O ID do produto.
     * @param canalVendaId O ID do canal de venda.
     * @return O DTO de resposta com os detalhes do estoque encontrado.
     */
    EstoqueResponseDTO consultarEstoque(Long produtoId, Long canalVendaId);

    /**
     * Lista todos os registros de estoque para um determinado produto, em todos os canais de venda.
     *
     * @param produtoId O ID do produto.
     * @return Uma lista de DTOs de resposta, cada um representando o estoque em um canal.
     */
    List<EstoqueResponseDTO> listarEstoquesPorProduto(Long produtoId);
}
