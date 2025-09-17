package com.dcriar.domain.product.service;

import com.dcriar.api.dto.request.product.ProdutoRequestDTO;
import com.dcriar.api.dto.response.product.ProdutoResponseDTO;

import java.util.List;

/**
 * Interface que define o contrato para a lógica de negócio de Produtos.
 * <p>
 * Desacopla o controller da implementação do serviço, permitindo maior flexibilidade
 * e facilitando os testes.
 */
public interface ProdutoService {

    /**
     * Busca todos os produtos cadastrados.
     *
     * @return Uma lista de DTOs de resposta de produtos.
     */
    List<ProdutoResponseDTO> findAll();

    /**
     * Busca um produto específico pelo seu ID.
     *
     * @param id O ID do produto a ser buscado.
     * @return O DTO de resposta do produto encontrado.
     */
    ProdutoResponseDTO findById(Long id);

    /**
     * Cria um novo produto no sistema.
     *
     * @param requestDTO O DTO com os dados para a criação.
     * @return O DTO de resposta do produto recém-criado.
     */
    ProdutoResponseDTO create(ProdutoRequestDTO requestDTO);

    /**
     * Atualiza um produto existente.
     *
     * @param id O ID do produto a ser atualizado.
     * @param requestDTO O DTO com os novos dados.
     * @return O DTO de resposta do produto atualizado.
     */
    ProdutoResponseDTO update(Long id, ProdutoRequestDTO requestDTO);

    /**
     * Deleta um produto pelo seu ID.
     *
     * @param id O ID do produto a ser deletado.
     */
    void deleteById(Long id);
}

