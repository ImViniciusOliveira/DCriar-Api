package com.dcriar.domain.product.service.impl;

import com.dcriar.api.dto.request.product.AjusteEstoqueProdutoRequestDTO;
import com.dcriar.api.dto.request.product.AjusteEstoqueRequestDTO;
import com.dcriar.api.dto.response.product.EstoqueResponseDTO;
import com.dcriar.api.dto.response.product.MovimentacaoProdutoResponseDTO;
import com.dcriar.api.mapper.product.EstoqueMapper;
import com.dcriar.api.mapper.product.MovimentacaoProdutoMapper;
import com.dcriar.domain.product.entity.CanalVenda;
import com.dcriar.domain.product.entity.Estoque;
import com.dcriar.domain.product.entity.MovimentacaoEstoqueProduto;
import com.dcriar.domain.product.entity.Produto;
import com.dcriar.domain.product.entity.enuns.TipoMovimentacaoProduto;
import com.dcriar.domain.product.repository.CanalVendaRepository;
import com.dcriar.domain.product.repository.EstoqueRepository;
import com.dcriar.domain.product.repository.MovimentacaoEstoqueProdutoRepository;
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
    private final MovimentacaoEstoqueProdutoRepository movimentacaoEstoqueProdutoRepository;
    private final MovimentacaoProdutoMapper movimentacaoProdutoMapper;

    @Override
    @Transactional
    public EstoqueResponseDTO ajustarEstoque(AjusteEstoqueRequestDTO requestDTO) {
        Produto produto = findProdutoById(requestDTO.getProdutoId());
        CanalVenda canalVenda = findCanalVendaById(requestDTO.getCanalVendaId());

        if (requestDTO.getQuantidade() > 0) {
            Integer estoqueFisicoTotal = movimentacaoEstoqueProdutoRepository.findSaldoByProduto(produto);
            List<Estoque> estoquesAtuais = estoqueRepository.findAllByProduto(produto);
            int totalDistribuido = estoquesAtuais.stream()
                    .mapToInt(Estoque::getQuantidade)
                    .sum();
            int novoTotalDistribuido = totalDistribuido + requestDTO.getQuantidade();

            if (novoTotalDistribuido > estoqueFisicoTotal) {
                throw new IllegalStateException(
                        "Operação bloqueada. O total distribuído (" + novoTotalDistribuido + ") não pode ultrapassar o estoque físico total (" + estoqueFisicoTotal + ")."
                );
            }
        }

        Estoque estoque = estoqueRepository.findByProdutoAndCanalVenda(produto, canalVenda)
                .orElseGet(() -> criarNovoEstoque(produto, canalVenda));

        int novaQuantidade = estoque.getQuantidade() + requestDTO.getQuantidade();
        if (novaQuantidade < 0) {
            throw new IllegalArgumentException("A operação resultaria em estoque negativo no canal.");
        }
        estoque.setQuantidade(novaQuantidade);

        Estoque estoqueSalvo = estoqueRepository.save(estoque);
        return estoqueMapper.toResponseDTO(estoqueSalvo);
    }

    @Override
    @Transactional
    public void ajustarEstoqueFisico(AjusteEstoqueProdutoRequestDTO requestDTO) {
        Produto produto = findProdutoById(requestDTO.getProdutoId());

        MovimentacaoEstoqueProduto movimentacaoManual = MovimentacaoEstoqueProduto.builder()
                .produto(produto)
                .tipo(TipoMovimentacaoProduto.AJUSTE_MANUAL)
                .quantidade(requestDTO.getQuantidade())
                .motivo(requestDTO.getMotivo())
                .build();

        movimentacaoEstoqueProdutoRepository.save(movimentacaoManual);
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

    @Override
    @Transactional(readOnly = true)
    public List<MovimentacaoProdutoResponseDTO> listarMovimentacoesPorProduto(Long produtoId) {
        Produto produto = findProdutoById(produtoId);
        return movimentacaoEstoqueProdutoRepository.findAllByProduto(produto)
                .stream()
                .map(movimentacaoProdutoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    private Estoque criarNovoEstoque(Produto produto, CanalVenda canalVenda) {
        return Estoque.builder()
                .produto(produto)
                .canalVenda(canalVenda)
                .quantidade(0)
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

