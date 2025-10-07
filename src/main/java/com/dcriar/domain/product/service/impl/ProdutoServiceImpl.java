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
import com.dcriar.domain.upload.service.FileStorageService;
import com.dcriar.exception.custom.ProdutoEmUsoException;
import com.dcriar.exception.custom.ProdutoInvalidoException;
import com.dcriar.exception.custom.ProdutoNotFoundException;
import com.dcriar.exception.custom.TipoMateriaPrimaNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementação da lógica de negócio para gerenciamento de Produtos ("moldes").
 * Esta classe é responsável por todas as operações de CRUD e regras de negócio
 * relacionadas aos produtos, como a criação, atualização, busca e exclusão,
 * garantindo a consistência dos dados e a integridade do estoque.
 */
@Service
@RequiredArgsConstructor
public class ProdutoServiceImpl implements ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final TipoMateriaPrimaRepository tipoMateriaPrimaRepository;
    private final MovimentacaoEstoqueProdutoRepository movimentacaoEstoqueProdutoRepository;
    private final EstoqueRepository estoqueRepository;
    private final OrdemDeCorteRepository ordemDeCorteRepository;
    private final ProdutoMapper produtoMapper;
    private final FileStorageService fileStorageService;

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

        // Garante que apenas o nome do arquivo seja salvo no banco de dados.
        if (produto.getFotoPrincipalUrl() != null && !produto.getFotoPrincipalUrl().isBlank()) {
            String fileName = fileStorageService.extractFileName(produto.getFotoPrincipalUrl());
            produto.setFotoPrincipalUrl(fileName);
        }

        Produto produtoSalvo = produtoRepository.save(produto);
        return mapAndEnrichProduto(produtoSalvo);
    }

    @Override
    @Transactional
    public ProdutoResponseDTO update(Long id, ProdutoRequestDTO requestDTO) {
        Produto produto = findProdutoById(id);
        String oldFotoFileName = produto.getFotoPrincipalUrl(); // Armazena o nome do arquivo antigo.

        validarRegrasDeNegocio(requestDTO, id);

        var tipoMateriaPrima = tipoMateriaPrimaRepository.findById(requestDTO.getTipoMateriaPrimaId())
                .orElseThrow(() -> new TipoMateriaPrimaNotFoundException(requestDTO.getTipoMateriaPrimaId()));

        produto.updateFrom(requestDTO);
        produto.setTipoMateriaPrima(tipoMateriaPrima);

        // Lógica para gerenciamento inteligente de imagens.
        String newFotoUrlFromDto = requestDTO.getFotoPrincipalUrl();

        if (newFotoUrlFromDto != null && !newFotoUrlFromDto.isBlank()) {
            // Se uma nova URL de imagem foi enviada, extrai apenas o nome do arquivo.
            String newFileName = fileStorageService.extractFileName(newFotoUrlFromDto);

            // Se o nome do arquivo novo for diferente do antigo, exclui o arquivo antigo.
            if (oldFotoFileName != null && !oldFotoFileName.equals(newFileName)) {
                fileStorageService.deleteFile(oldFotoFileName);
            }
            // Salva apenas o nome do novo arquivo no banco de dados.
            produto.setFotoPrincipalUrl(newFileName);
        } else {
            // Se a URL da imagem no DTO for nula ou em branco, significa que a imagem deve ser removida.
            if (oldFotoFileName != null && !oldFotoFileName.isBlank()) {
                fileStorageService.deleteFile(oldFotoFileName);
            }
            produto.setFotoPrincipalUrl(null); // Remove a referência no banco de dados.
        }

        Produto produtoAtualizado = produtoRepository.save(produto);
        return mapAndEnrichProduto(produtoAtualizado);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Produto produto = findProdutoById(id);

        List<com.dcriar.domain.production.entity.OrdemDeCorte> ordens = ordemDeCorteRepository.findAllByProduto(produto);
        if (!ordens.isEmpty()) {
            Set<Long> ordemIds = ordens.stream().map(com.dcriar.domain.production.entity.OrdemDeCorte::getId).collect(Collectors.toSet());
            throw new ProdutoEmUsoException(id, ordemIds);
        }

        // Exclui a imagem associada ao produto, se houver.
        if (produto.getFotoPrincipalUrl() != null && !produto.getFotoPrincipalUrl().isBlank()) {
            fileStorageService.deleteFile(produto.getFotoPrincipalUrl());
        }

        produtoRepository.delete(produto);
    }

    /**
     * Mapeia uma entidade {@link Produto} para um {@link ProdutoResponseDTO} e enriquece
     * o DTO com informações de estoque e a URL completa da imagem.
     */
    private ProdutoResponseDTO mapAndEnrichProduto(Produto produto) {
        ProdutoResponseDTO dto = produtoMapper.toResponseDTO(produto);

        // Constrói a URL de download completa da imagem dinamicamente.
        if (dto.getFotoPrincipalUrl() != null && !dto.getFotoPrincipalUrl().isBlank()) {
            String fileName = dto.getFotoPrincipalUrl(); // Contém apenas o nome do arquivo.
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

        if (produtoId == null) { // Operação de criação
            if (produtoRepository.existsByNome(requestDTO.getNome())) {
                errors.put("nome", String.format("Já existe um produto com o nome '%s'.", requestDTO.getNome()));
            }
            if (produtoRepository.existsBySku(requestDTO.getSku())) {
                errors.put("sku", String.format("Já existe um produto com o SKU '%s'.", requestDTO.getSku()));
            }
        } else { // Operação de atualização
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
