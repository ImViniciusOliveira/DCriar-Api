package com.dcriar.domain.product.service.impl;

import com.dcriar.api.dto.request.product.ProdutoRequestDTO;
import com.dcriar.api.dto.response.product.ProdutoResponseDTO;
import com.dcriar.api.mapper.product.ProdutoMapper;
import com.dcriar.domain.product.entity.Estoque;
import com.dcriar.domain.product.entity.Produto;
import com.dcriar.domain.product.repository.EstoqueRepository;
import com.dcriar.domain.product.repository.MovimentacaoEstoqueProdutoRepository;
import com.dcriar.domain.product.repository.ProdutoRepository;
import com.dcriar.domain.production.repository.OrdemDeCorteRepository;
import com.dcriar.domain.stock.repository.TipoMateriaPrimaRepository;
import com.dcriar.domain.product.service.ProdutoService;
import com.dcriar.exception.custom.ProdutoEmUsoException;
import com.dcriar.exception.custom.ProdutoInvalidoException;
import com.dcriar.exception.custom.ProdutoNotFoundException;
import com.dcriar.exception.custom.TipoMateriaPrimaNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final OrdemDeCorteRepository ordemDeCorteRepository;

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
        validarRegrasDeNegocio(requestDTO, null);

        var tipoMateriaPrima = tipoMateriaPrimaRepository.findById(requestDTO.getTipoMateriaPrimaId())
                .orElseThrow(() -> new TipoMateriaPrimaNotFoundException(requestDTO.getTipoMateriaPrimaId()));

        Produto produto = Produto.from(requestDTO);
        produto.setTipoMateriaPrima(tipoMateriaPrima);

        Produto produtoSalvo = produtoRepository.save(produto);
        return mapAndEnrichProduto(produtoSalvo);
    }

    @Override
    @Transactional
    public ProdutoResponseDTO update(Long id, ProdutoRequestDTO requestDTO) {
        Produto produto = findProdutoById(id);

        validarRegrasDeNegocio(requestDTO, id);

        var tipoMateriaPrima = tipoMateriaPrimaRepository.findById(requestDTO.getTipoMateriaPrimaId())
                .orElseThrow(() -> new TipoMateriaPrimaNotFoundException(requestDTO.getTipoMateriaPrimaId()));

        produto.updateFrom(requestDTO);
        produto.setTipoMateriaPrima(tipoMateriaPrima);

        Produto produtoAtualizado = produtoRepository.save(produto);
        return mapAndEnrichProduto(produtoAtualizado);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        // Verifica se o produto existe, lança ProdutoNotFoundException se não existir
        findProdutoById(id);

        // Verifica se o produto está sendo usado em alguma ordem de corte
        if (ordemDeCorteRepository.existsByProdutoId(id)) {
            throw new ProdutoEmUsoException(id);
        }

        // Pode deletar
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

    private void validarRegrasDeNegocio(ProdutoRequestDTO requestDTO, Long produtoId) {
        Map<String, String> errors = new HashMap<>();

        if (produtoId == null) {
            if (produtoRepository.existsByNome(requestDTO.getNome())) {
                errors.put("nome", "Já existe um produto com esse nome");
            }
            if (produtoRepository.existsBySku(requestDTO.getSku())) {
                errors.put("sku", "Já existe um produto com esse SKU");
            }
        } else {
            if (produtoRepository.existsByNomeAndIdNot(requestDTO.getNome(), produtoId)) {
                errors.put("nome", "Já existe outro produto com esse nome");
            }
            if (produtoRepository.existsBySkuAndIdNot(requestDTO.getSku(), produtoId)) {
                errors.put("sku", "Já existe outro produto com esse SKU");
            }
        }

        if (!errors.isEmpty()) {
            throw new ProdutoInvalidoException("Erros de validação de negócio encontrados", errors);
        }
    }
}
