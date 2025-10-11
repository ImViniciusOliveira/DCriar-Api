package com.dcriar.domain.product.service;

import com.dcriar.api.dto.request.product.ProdutoRequestDTO;
import com.dcriar.api.dto.response.product.ProdutoResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * Interface que define o contrato para a lógica de negócio de Produtos.
 * Desacopla o controller da implementação do serviço, permitindo maior flexibilidade
 * e facilitando os testes.
 */
public interface ProdutoService {

    /**
     * Busca todos os produtos cadastrados de forma paginada.
     *
     * @param pageable Objeto com as informações de paginação (página, tamanho, ordenação).
     * @return Uma página de DTOs de resposta de produtos.
     */
    Page<ProdutoResponseDTO> findAll(Pageable pageable);

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
     * Atualiza um produto existente a partir de um DTO completo.
     *
     * @param id O ID do produto a ser atualizado.
     * @param requestDTO O DTO com os novos dados.
     * @return O DTO de resposta do produto atualizado.
     */
    ProdutoResponseDTO update(Long id, ProdutoRequestDTO requestDTO);

    /**
     * Atualiza parcialmente um produto existente a partir de um mapa de campos.
     *
     * @param id O ID do produto a ser atualizado.
     * @param fields Um mapa contendo os nomes dos campos e seus novos valores.
     * @return O DTO de resposta do produto atualizado.
     */
    ProdutoResponseDTO patch(Long id, Map<String, Object> fields);

    /**
     * Deleta um produto pelo seu ID.
     *
     * @param id O ID do produto a ser deletado.
     */
    void deleteById(Long id);
}
