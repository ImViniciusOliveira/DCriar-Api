package com.dcriar.domain.product.service.impl;

import com.dcriar.api.dto.request.product.ProdutoRequestDTO;
import com.dcriar.api.dto.response.product.ProdutoResponseDTO;
import com.dcriar.api.mapper.product.ProdutoMapper;
import com.dcriar.domain.product.entity.Estoque;
import com.dcriar.domain.product.entity.Produto;
import com.dcriar.domain.product.repository.EstoqueRepository;
import com.dcriar.domain.product.repository.MovimentacaoEstoqueProdutoRepository;
import com.dcriar.domain.product.repository.ProdutoRepository;
import com.dcriar.domain.production.entity.OrdemDeProducao;
import com.dcriar.domain.production.repository.OrdemDeProducaoRepository;
import com.dcriar.domain.stock.repository.TipoMateriaPrimaRepository;
import com.dcriar.domain.product.service.ProdutoService;
import com.dcriar.domain.upload.service.FileStorageService;
import com.dcriar.exception.custom.ProdutoEmUsoException;
import com.dcriar.exception.custom.ProdutoInvalidoException;
import com.dcriar.exception.custom.ProdutoNotFoundException;
import com.dcriar.exception.custom.TipoMateriaPrimaNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProdutoServiceImpl implements ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final TipoMateriaPrimaRepository tipoMateriaPrimaRepository;
    private final MovimentacaoEstoqueProdutoRepository movimentacaoEstoqueProdutoRepository;
    private final EstoqueRepository estoqueRepository;
    private final OrdemDeProducaoRepository ordemDeProducaoRepository;
    private final ProdutoMapper produtoMapper;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @Override
    @Transactional(readOnly = true)
    public Page<ProdutoResponseDTO> findAll(Pageable pageable) {
        Page<Produto> produtoPage = produtoRepository.findAll(pageable);
        return produtoPage.map(this::mapAndEnrichProduto);
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

        if (produto.getFotoPrincipalUrl() != null && !produto.getFotoPrincipalUrl().isBlank()) {
            String fileName = fileStorageService.extractFileName(produto.getFotoPrincipalUrl());
            produto.setFotoPrincipalUrl(fileName);
        }

        Produto produtoSalvo = produtoRepository.save(produto);

        return findById(produtoSalvo.getId());
    }

    @Override
    @Transactional
    public ProdutoResponseDTO update(Long id, ProdutoRequestDTO requestDTO) {
        Produto produto = findProdutoById(id);
        String oldFotoFileName = produto.getFotoPrincipalUrl();

        validarRegrasDeNegocio(requestDTO, id);

        var tipoMateriaPrima = tipoMateriaPrimaRepository.findById(requestDTO.getTipoMateriaPrimaId())
                .orElseThrow(() -> new TipoMateriaPrimaNotFoundException(requestDTO.getTipoMateriaPrimaId()));

        produto.updateFrom(requestDTO);
        produto.setTipoMateriaPrima(tipoMateriaPrima);

        String newFotoUrlFromDto = requestDTO.getFotoPrincipalUrl();

        if (newFotoUrlFromDto != null && !newFotoUrlFromDto.isBlank()) {
            String newFileName = fileStorageService.extractFileName(newFotoUrlFromDto);

            if (oldFotoFileName != null && !oldFotoFileName.equals(newFileName)) {
                fileStorageService.deleteFile(oldFotoFileName);
            }
            produto.setFotoPrincipalUrl(newFileName);
        } else {
            if (oldFotoFileName != null && !oldFotoFileName.isBlank()) {
                fileStorageService.deleteFile(oldFotoFileName);
            }
            produto.setFotoPrincipalUrl(null);
        }

        produtoRepository.save(produto);

        return findById(id);
    }

    @Override
    @Transactional
    public ProdutoResponseDTO patch(Long id, Map<String, Object> fields) {
        // Esta implementação foi movida para ProdutoServiceImpl
        return null;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Produto produto = findProdutoById(id);

        List<OrdemDeProducao> ordens = ordemDeProducaoRepository.findAllByProduto(produto);
        if (!ordens.isEmpty()) {
            Set<Long> ordemIds = ordens.stream().map(OrdemDeProducao::getId).collect(Collectors.toSet());
            throw new ProdutoEmUsoException(id, ordemIds);
        }

        if (produto.getFotoPrincipalUrl() != null && !produto.getFotoPrincipalUrl().isBlank()) {
            fileStorageService.deleteFile(produto.getFotoPrincipalUrl());
        }

        produtoRepository.delete(produto);
    }

    private ProdutoResponseDTO mapAndEnrichProduto(Produto produto) {
        ProdutoResponseDTO dto = produtoMapper.toResponseDTO(produto);

        if (dto.getFotoPrincipalUrl() != null && !dto.getFotoPrincipalUrl().isBlank()) {
            String fileName = dto.getFotoPrincipalUrl();
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/v1/uploads/")
                    .path(fileName)
                    .toUriString();
            dto.setFotoPrincipalUrl(fileDownloadUri);
        }

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
                errors.put("nome", String.format("Já existe um produto com o nome '%s'.", requestDTO.getNome()));
            }
            if (produtoRepository.existsBySku(requestDTO.getSku())) {
                errors.put("sku", String.format("Já existe um produto com o SKU '%s'.", requestDTO.getSku()));
            }
        } else {
            if (produtoRepository.existsByNomeAndIdNot(requestDTO.getNome(), produtoId)) {
                errors.put("nome", String.format("Já existe outro produto com o nome '%s'.", requestDTO.getNome()));
            }
            if (produtoRepository.existsBySkuAndIdNot(requestDTO.getSku(), produtoId)) {
                errors.put("sku", String.format("Já existe outro produto com o SKU '%s'.", requestDTO.getSku()));
            }
        }

        if (!errors.isEmpty()) {
            throw new ProdutoInvalidoException("Erros de validação de negócio encontrados", errors);
        }
    }
}
