package com.dcriar.domain.product.service.impl;

import com.dcriar.api.dto.request.product.ProdutoRequestDTO;
import com.dcriar.api.dto.response.product.ProdutoResponseDTO;
import com.dcriar.api.mapper.product.ProdutoMapper;
import com.dcriar.domain.product.entity.Estoque;
import com.dcriar.domain.product.entity.Produto;
import com.dcriar.domain.product.repository.EstoqueRepository;
import com.dcriar.domain.product.repository.MovimentacaoEstoqueProdutoRepository;
import com.dcriar.domain.product.repository.ProdutoRepository;
import com.dcriar.domain.stock.entity.TipoMateriaPrima;
import com.dcriar.domain.stock.repository.TipoMateriaPrimaRepository;
import com.dcriar.domain.product.service.ProdutoService;
import com.dcriar.exception.custom.ProdutoNotFoundException;
import com.dcriar.exception.custom.RegraNegocioException;
import com.dcriar.exception.custom.TipoMateriaPrimaNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementação da lógica de negócio para gerenciamento de Produtos ("moldes").
 */
@Service
@RequiredArgsConstructor
public class ProdutoServiceImpl implements ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final TipoMateriaPrimaRepository tipoMateriaPrimaRepository;
    private final ProdutoMapper produtoMapper;
    private final MovimentacaoEstoqueProdutoRepository movimentacaoEstoqueProdutoRepository;
    private final EstoqueRepository estoqueRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProdutoResponseDTO> findAll() {
        return produtoRepository.findAll().stream()
                .map(this::mapAndEnrichProduto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProdutoResponseDTO findById(Long id) {
        Produto produto = findProdutoById(id);
        return mapAndEnrichProduto(produto);
    }

    @Override
    @Transactional
    public ProdutoResponseDTO create(ProdutoRequestDTO requestDTO) {
        validarNomeESkuUnicos(requestDTO.getNome(), requestDTO.getSku());

        // 1. Busca a dependência (TipoMateriaPrima)
        TipoMateriaPrima tipoMateriaPrima = tipoMateriaPrimaRepository.findById(requestDTO.getTipoMateriaPrimaId())
                .orElseThrow(() -> new TipoMateriaPrimaNotFoundException(requestDTO.getTipoMateriaPrimaId()));

        // 2. Cria a entidade a partir do DTO
        Produto produto = Produto.from(requestDTO);

        // 3. Define a relação
        produto.setTipoMateriaPrima(tipoMateriaPrima);

        Produto produtoSalvo = produtoRepository.save(produto);
        return mapAndEnrichProduto(produtoSalvo);
    }

    @Override
    @Transactional
    public ProdutoResponseDTO update(Long id, ProdutoRequestDTO requestDTO) {
        Produto produto = findProdutoById(id);
        validarNomeESkuUnicosParaUpdate(id, requestDTO.getNome(), requestDTO.getSku());

        // 1. Busca a nova dependência (TipoMateriaPrima)
        TipoMateriaPrima tipoMateriaPrima = tipoMateriaPrimaRepository.findById(requestDTO.getTipoMateriaPrimaId())
                .orElseThrow(() -> new TipoMateriaPrimaNotFoundException(requestDTO.getTipoMateriaPrimaId()));

        // 2. Atualiza os dados da entidade a partir do DTO
        produto.updateFrom(requestDTO);

        // 3. Define a nova relação
        produto.setTipoMateriaPrima(tipoMateriaPrima);

        Produto produtoAtualizado = produtoRepository.save(produto);
        return mapAndEnrichProduto(produtoAtualizado);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!produtoRepository.existsById(id)) {
            throw new ProdutoNotFoundException(id);
        }
        produtoRepository.deleteById(id);
    }

    /* ============================
       Métodos auxiliares privados
       ============================ */

    private ProdutoResponseDTO mapAndEnrichProduto(Produto produto) {
        ProdutoResponseDTO dto = produtoMapper.toResponseDTO(produto);

        Integer estoqueFisicoTotal = movimentacaoEstoqueProdutoRepository.findSaldoByProduto(produto);
        List<Estoque> estoquesAtuais = estoqueRepository.findAllByProduto(produto);
        int estoqueDistribuidoTotal = estoquesAtuais.stream()
                .mapToInt(Estoque::getQuantidade)
                .sum();
        int estoqueDisponivelParaAlocar = estoqueFisicoTotal - estoqueDistribuidoTotal;

        dto.setEstoqueFisicoTotal(estoqueFisicoTotal);
        dto.setEstoqueDistribuidoTotal(estoqueDistribuidoTotal);
        dto.setEstoqueDisponivelParaAlocar(estoqueDisponivelParaAlocar);

        return dto;
    }

    private Produto findProdutoById(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ProdutoNotFoundException(id));
    }

    private void validarNomeESkuUnicos(String nome, String sku) {
        if (produtoRepository.existsByNome(nome)) {
            throw new RegraNegocioException("Já existe um produto com esse nome: " + nome);
        }
        if (produtoRepository.existsBySku(sku)) {
            throw new RegraNegocioException("Já existe um produto com esse SKU: " + sku);
        }
    }

    private void validarNomeESkuUnicosParaUpdate(Long id, String nome, String sku) {
        if (produtoRepository.existsByNomeAndIdNot(nome, id)) {
            throw new RegraNegocioException("Já existe outro produto com esse nome: " + nome);
        }
        if (produtoRepository.existsBySkuAndIdNot(sku, id)) {
            throw new RegraNegocioException("Já existe outro produto com esse SKU: " + sku);
        }
    }
}

