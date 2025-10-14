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

/**
 * Implementação do serviço de gerenciamento de produtos.
 * <p>
 * Esta classe contém a lógica de negócio para operações de CRUD em produtos,
 * além de orquestrar validações, manipulação de arquivos de imagem e
 * enriquecimento dos dados de resposta com informações de estoque.
 */
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

    /**
     * {@inheritDoc}
     * <p>
     * Cada produto na página de resposta é enriquecido com informações de estoque
     * e a URL completa para a foto principal.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProdutoResponseDTO> findAll(Pageable pageable) {
        Page<Produto> produtoPage = produtoRepository.findAll(pageable);
        return produtoPage.map(this::mapAndEnrichProduto);
    }

    /**
     * {@inheritDoc}
     * <p>
     * O produto retornado é enriquecido com informações de estoque
     * e a URL completa para a foto principal.
     */
    @Override
    @Transactional(readOnly = true)
    public ProdutoResponseDTO findById(Long id) {
        Produto produto = findProdutoById(id);
        return mapAndEnrichProduto(produto);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Antes de salvar, o método valida regras de negócio (nome e SKU únicos),
     * associa o tipo de matéria-prima e processa a URL da imagem do produto,
     * extraindo apenas o nome do arquivo para armazenamento.
     *
     * @throws TipoMateriaPrimaNotFoundException se o tipo de matéria-prima especificado não for encontrado.
     * @throws ProdutoInvalidoException se as regras de negócio (nome/SKU único) forem violadas.
     */
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

    /**
     * {@inheritDoc}
     * <p>
     * Realiza a validação de regras de negócio (nome e SKU únicos, ignorando o próprio produto).
     * Gerencia o ciclo de vida da foto do produto: se uma nova foto for fornecida,
     * a antiga é excluída do armazenamento. Se a URL da foto for removida, o arquivo
     * correspondente também é excluído.
     *
     * @throws ProdutoNotFoundException se o produto com o ID fornecido não for encontrado.
     * @throws TipoMateriaPrimaNotFoundException se o tipo de matéria-prima especificado não for encontrado.
     * @throws ProdutoInvalidoException se as regras de negócio (nome/SKU único) forem violadas.
     */
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

    /**
     * {@inheritDoc}
     * <p>
     * <b>Atenção:</b> A lógica para atualização parcial (PATCH) ainda não foi implementada.
     * O método atualmente retorna {@code null}.
     *
     * @param id O ID do produto a ser atualizado.
     * @param fields Um mapa contendo os nomes dos campos e seus novos valores.
     * @return Atualmente {@code null}. Deveria retornar o DTO de resposta do produto atualizado.
     */
    @Override
    @Transactional
    public ProdutoResponseDTO patch(Long id, Map<String, Object> fields) {
        // Esta implementação foi movida para ProdutoServiceImpl
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Antes de excluir, o método verifica se o produto está em uso em alguma Ordem de Produção.
     * Se estiver, a exclusão é impedida. Se o produto tiver uma foto associada,
     * o arquivo correspondente é excluído do armazenamento.
     *
     * @throws ProdutoNotFoundException se o produto com o ID fornecido não for encontrado.
     * @throws ProdutoEmUsoException se o produto estiver vinculado a uma ou mais ordens de produção.
     */
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

    /**
     * Mapeia uma entidade {@link Produto} para seu {@link ProdutoResponseDTO} e o enriquece com dados adicionais.
     * <p>
     * Este método realiza as seguintes ações:
     * <ol>
     *     <li>Converte a entidade Produto para ProdutoResponseDTO.</li>
     *     <li>Se houver uma foto, constrói a URL de download completa.</li>
     *     <li>Calcula o estoque físico total, o total distribuído entre os canais e o saldo disponível para alocação.</li>
     *     <li>Adiciona essas informações de estoque ao DTO.</li>
     * </ol>
     *
     * @param produto A entidade {@link Produto} a ser processada.
     * @return O {@link ProdutoResponseDTO} enriquecido.
     */
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

    /**
     * Busca uma entidade {@link Produto} pelo seu ID, lançando uma exceção se não for encontrada.
     *
     * @param id O ID do produto a ser buscado.
     * @return A entidade {@link Produto} encontrada.
     * @throws ProdutoNotFoundException se o produto com o ID especificado não for encontrado.
     */
    private Produto findProdutoById(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ProdutoNotFoundException(id));
    }

    /**
     * Valida regras de negócio para a criação e atualização de produtos, como a unicidade de nome e SKU.
     *
     * @param requestDTO O DTO com os dados do produto.
     * @param produtoId O ID do produto que está sendo atualizado, ou {@code null} se for uma criação.
     * @throws ProdutoInvalidoException se qualquer uma das regras de negócio for violada. A exceção contém um mapa com os campos e as mensagens de erro.
     */
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
