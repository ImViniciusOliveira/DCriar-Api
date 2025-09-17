package com.dcriar.domain.product.service.impl;

import com.dcriar.api.dto.request.product.ProdutoRequestDTO;
import com.dcriar.api.dto.response.product.ProdutoResponseDTO;
import com.dcriar.api.mapper.product.ProdutoMapper;
import com.dcriar.domain.product.entity.ComposicaoProduto;
import com.dcriar.domain.product.entity.Produto;
import com.dcriar.domain.stock.entity.TipoMateriaPrima;
import com.dcriar.domain.product.repository.ProdutoRepository;
import com.dcriar.domain.stock.repository.TipoMateriaPrimaRepository;
import com.dcriar.domain.product.service.ProdutoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementação da lógica de negócio para gerenciamento de Produtos.
 */
@Service
@RequiredArgsConstructor
public class ProdutoServiceImpl implements ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final TipoMateriaPrimaRepository tipoMateriaPrimaRepository;
    private final ProdutoMapper produtoMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProdutoResponseDTO> findAll() {
        return produtoRepository.findAll().stream()
                .map(produtoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProdutoResponseDTO findById(Long id) {
        Produto produto = findProdutoById(id);
        return produtoMapper.toResponseDTO(produto);
    }

    @Override
    @Transactional
    public ProdutoResponseDTO create(ProdutoRequestDTO requestDTO) {
        Produto produto = Produto.from(requestDTO);
        atualizarComposicaoDoProduto(produto, requestDTO.getComposicao());
        Produto produtoSalvo = produtoRepository.save(produto);
        return produtoMapper.toResponseDTO(produtoSalvo);
    }

    @Override
    @Transactional
    public ProdutoResponseDTO update(Long id, ProdutoRequestDTO requestDTO) {
        Produto produto = findProdutoById(id);
        produto.updateFrom(requestDTO);
        produto.limparComposicao();
        atualizarComposicaoDoProduto(produto, requestDTO.getComposicao());
        Produto produtoAtualizado = produtoRepository.save(produto);
        return produtoMapper.toResponseDTO(produtoAtualizado);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!produtoRepository.existsById(id)) {
            throw new EntityNotFoundException("Produto não encontrado com o ID: " + id);
        }
        produtoRepository.deleteById(id);
    }

    private Produto findProdutoById(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com o ID: " + id));
    }

    private void atualizarComposicaoDoProduto(Produto produto, Set<ProdutoRequestDTO.ComposicaoRequestDTO> composicaoRequest) {
        if (composicaoRequest == null || composicaoRequest.isEmpty()) {
            return;
        }

        for (ProdutoRequestDTO.ComposicaoRequestDTO itemDTO : composicaoRequest) {
            TipoMateriaPrima tipoMateriaPrima = tipoMateriaPrimaRepository.findById(itemDTO.getMateriaPrimaId())
                    .orElseThrow(() -> new EntityNotFoundException("Tipo de Matéria-Prima não encontrado com o ID: " + itemDTO.getMateriaPrimaId()));

            ComposicaoProduto itemComposicao = ComposicaoProduto.builder()
                    .tipoMateriaPrima(tipoMateriaPrima)
                    .gastoMaterialPorUnidade(itemDTO.getGastoMaterialPorUnidade())
                    .build();
            produto.adicionarComposicao(itemComposicao);
        }
    }
}

