package com.dcriar.domain.product.service.impl;

import com.dcriar.api.dto.request.product.AjusteEstoqueRequestDTO;
import com.dcriar.api.dto.response.product.EstoqueResponseDTO;
import com.dcriar.api.mapper.product.EstoqueMapper;
import com.dcriar.domain.product.entity.CanalVenda;
import com.dcriar.domain.product.entity.Estoque;
import com.dcriar.domain.product.entity.Produto;
import com.dcriar.domain.product.repository.CanalVendaRepository;
import com.dcriar.domain.product.repository.EstoqueRepository;
import com.dcriar.domain.product.repository.ProdutoRepository;
import com.dcriar.domain.product.service.EstoqueProdutoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementação da lógica de negócio para Estoque de Produtos Acabados.
 */
@Service
@RequiredArgsConstructor
public class EstoqueProdutoServiceImpl implements EstoqueProdutoService {

    private final EstoqueRepository estoqueRepository;
    private final ProdutoRepository produtoRepository;
    private final CanalVendaRepository canalVendaRepository;
    private final EstoqueMapper estoqueMapper;

    @Override
    @Transactional
    public EstoqueResponseDTO ajustarEstoque(AjusteEstoqueRequestDTO requestDTO) {
        Produto produto = findProdutoById(requestDTO.getProdutoId());
        CanalVenda canalVenda = findCanalVendaById(requestDTO.getCanalVendaId());

        // Procura por um registro de estoque existente ou cria um novo.
        Estoque estoque = estoqueRepository.findByProdutoAndCanalVenda(produto, canalVenda)
                .orElseGet(() -> criarNovoEstoque(produto, canalVenda));

        // Ajusta a quantidade.
        int novaQuantidade = estoque.getQuantidade() + requestDTO.getQuantidade();
        if (novaQuantidade < 0) {
            throw new IllegalArgumentException("A operação resultaria em estoque negativo.");
        }
        estoque.setQuantidade(novaQuantidade);

        Estoque estoqueSalvo = estoqueRepository.save(estoque);
        return estoqueMapper.toResponseDTO(estoqueSalvo);
    }

    @Override
    @Transactional(readOnly = true)
    public EstoqueResponseDTO consultarEstoque(Long produtoId, Long canalVendaId) {
        Produto produto = findProdutoById(produtoId);
        CanalVenda canalVenda = findCanalVendaById(canalVendaId);

        return estoqueRepository.findByProdutoAndCanalVenda(produto, canalVenda)
                .map(estoqueMapper::toResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Nenhum registro de estoque encontrado para o produto ID " + produtoId + " no canal de venda ID " + canalVendaId
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstoqueResponseDTO> listarEstoquesPorProduto(Long produtoId) {
        Produto produto = findProdutoById(produtoId);
        return estoqueRepository.findAllByProduto(produto).stream()
                .map(estoqueMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    private Estoque criarNovoEstoque(Produto produto, CanalVenda canalVenda) {
        return Estoque.builder()
                .produto(produto)
                .canalVenda(canalVenda)
                .quantidade(0) // Começa com zero antes do primeiro ajuste.
                .build();
    }

    private Produto findProdutoById(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com o ID: " + id));
    }

    private CanalVenda findCanalVendaById(Long id) {
        return canalVendaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Canal de Venda não encontrado com o ID: " + id));
    }
}
