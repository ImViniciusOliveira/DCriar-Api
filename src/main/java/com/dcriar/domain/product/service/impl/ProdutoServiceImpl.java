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
import com.dcriar.exception.custom.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
    private final ObjectMapper objectMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProdutoResponseDTO> findAll(Pageable pageable) {
        Page<Produto> produtoPage = produtoRepository.findAll(pageable);
        return produtoPage.map(this::mapAndEnrichProduto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public ProdutoResponseDTO findById(Long id) {
        Produto produto = findProdutoById(id);
        return mapAndEnrichProduto(produto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ProdutoResponseDTO create(ProdutoRequestDTO requestDTO) {
        Map<String, String> errors = validarCamposObrigatorios(requestDTO);
        if (!errors.isEmpty()) {
            throw new ProdutoInvalidoException("Dados do produto inválidos", errors);
        }
        validarRegrasDeNegocio(requestDTO, null);

        var tipoMateriaPrima = tipoMateriaPrimaRepository.findById(requestDTO.getTipoMateriaPrimaId())
                .orElseThrow(() -> new TipoMateriaPrimaNaoEncontradoException(requestDTO.getTipoMateriaPrimaId()));

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
     */
    @Override
    @Transactional
    public ProdutoResponseDTO update(Long id, ProdutoRequestDTO requestDTO) {
        Map<String, String> errors = validarCamposObrigatorios(requestDTO);
        if (!errors.isEmpty()) {
            throw new ProdutoInvalidoException("Dados do produto inválidos", errors);
        }
        Produto produto = findProdutoById(id);
        String oldFotoFileName = produto.getFotoPrincipalUrl();

        validarRegrasDeNegocio(requestDTO, id);

        var tipoMateriaPrima = tipoMateriaPrimaRepository.findById(requestDTO.getTipoMateriaPrimaId())
                .orElseThrow(() -> new TipoMateriaPrimaNaoEncontradoException(requestDTO.getTipoMateriaPrimaId()));

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
     */
    @Override
    @Transactional
    public ProdutoResponseDTO patch(Long id, Map<String, Object> fields) {
        // Passo 1: Se o mapa de campos estiver vazio, nenhuma alteração é necessária.
        // Retorna o recurso atual para evitar um 404 e indicar sucesso sem modificação.
        if (fields == null || fields.isEmpty()) {
            return this.findById(id);
        }

        // Passo 2: Busca o estado atual do produto para usar como base para a fusão.
        Produto produtoAtual = findProdutoById(id);

        // Passo 3: Traduz os nomes de campos do frontend (contrato de resposta) para os nomes
        // esperados pelo backend (contrato de requisição).
        // TODO: (Refatoração) Mover a lógica de 'translateFieldNames' para a camada de Controller. A camada de serviço não deveria ter conhecimento sobre os nomes de campos específicos do contrato de resposta do frontend. O ideal é que essa tradução ocorra no Controller antes de chamar o serviço, ou usando um DTO específico para a operação PATCH.
        translateFieldNames(fields);

        // Passo 4: Converte a entidade atual em um mapa, de forma segura, para servir de base.
        Map<String, Object> produtoAsMap = objectMapper.convertValue(produtoAtual, new TypeReference<>() {});

        // Passo 5: Mescla os campos alterados (fields) sobre o estado atual (produtoAsMap).
        produtoAsMap.putAll(fields);

        // Passo 6: Converte o mapa final de volta para um DTO de requisição, pronto para ser processado.
        ProdutoRequestDTO requestDTO = objectMapper.convertValue(produtoAsMap, ProdutoRequestDTO.class);

        // Passo 7: Delega para o método update, reutilizando toda a lógica de negócio e validações.
        return update(id, requestDTO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ProdutoResponseDTO uploadFoto(Long produtoId, MultipartFile file) {
        // Passo 1: Busca o produto para garantir que ele existe e para obter o nome do arquivo antigo.
        Produto produto = findProdutoById(produtoId);
        String oldFotoFileName = produto.getFotoPrincipalUrl();

        // Passo 2: Delega o armazenamento do novo arquivo para o serviço de storage.
        String newFileName = fileStorageService.storeFile(file);

        // Passo 3: Associa o nome do novo arquivo à entidade do produto.
        produto.setFotoPrincipalUrl(newFileName);

        // Passo 4: Salva a entidade atualizada no banco de dados.
        Produto produtoAtualizado = produtoRepository.save(produto);

        // Passo 5: Após o sucesso da persistência, exclui o arquivo antigo para evitar inconsistências.
        if (oldFotoFileName != null && !oldFotoFileName.isBlank()) {
            fileStorageService.deleteFile(oldFotoFileName);
        }

        // Passo 6: Retorna o DTO enriquecido com a nova URL completa da foto.
        return mapAndEnrichProduto(produtoAtualizado);
    }

    /**
     * {@inheritDoc}
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
     * @throws ProdutoNaoEncontradoException se o produto com o ID especificado não for encontrado.
     */
    private Produto findProdutoById(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ProdutoNaoEncontradoException(id));
    }

    /**
     * Valida regras de negócio para a criação e atualização de produtos, como a unicidade de nome e SKU.
     *
     * @param requestDTO O DTO com os dados do produto.
     * @param produtoId O ID do produto que está sendo atualizado, ou {@code null} se for uma criação.
     * @throws ProdutoNomeDuplicadoException se o nome do produto já existir.
     * @throws ProdutoSkuDuplicadoException se o SKU do produto já existir.
     */
    private void validarRegrasDeNegocio(ProdutoRequestDTO requestDTO, Long produtoId) {
        if (produtoId == null) {
            if (produtoRepository.existsByNome(requestDTO.getNome())) {
                throw new ProdutoNomeDuplicadoException(requestDTO.getNome());
            }
            if (produtoRepository.existsBySku(requestDTO.getSku())) {
                throw new ProdutoSkuDuplicadoException(requestDTO.getSku());
            }
        } else {
            if (produtoRepository.existsByNomeAndIdNot(requestDTO.getNome(), produtoId)) {
                throw new ProdutoNomeDuplicadoException(requestDTO.getNome());
            }
            if (produtoRepository.existsBySkuAndIdNot(requestDTO.getSku(), produtoId)) {
                throw new ProdutoSkuDuplicadoException(requestDTO.getSku());
            }
        }
    }

    /**
     * Valida campos obrigatórios do produto.
     * @param requestDTO DTO do produto
     * @return Mapa de erros (campo -> mensagem)
     */
    private Map<String, String> validarCamposObrigatorios(ProdutoRequestDTO requestDTO) {
        Map<String, String> errors = new HashMap<>();
        if (requestDTO.getNome() == null || requestDTO.getNome().isBlank()) {
            errors.put("nome", "Nome do produto é obrigatório");
        }
        if (requestDTO.getSku() == null || requestDTO.getSku().isBlank()) {
            errors.put("sku", "SKU do produto é obrigatório");
        }
        if (requestDTO.getTipoMateriaPrimaId() == null) {
            errors.put("tipoMateriaPrimaId", "Tipo de matéria-prima é obrigatório");
        }
        return errors;
    }

    /**
     * Traduz os nomes de campos do payload do frontend para os nomes esperados pelo backend.
     * Este método modifica o mapa de campos diretamente.
     *
     * @param fields O mapa de campos recebido na requisição PATCH.
     */
    private void translateFieldNames(Map<String, Object> fields) {
        if (fields.containsKey("materiaPrima")) {
            Object value = fields.remove("materiaPrima");
            if (value instanceof Map) {
                Object id = ((Map<?, ?>) value).get("id");
                if (id != null) {
                    fields.put("tipoMateriaPrimaId", ((Number) id).longValue());
                }
            }
        }

        if (fields.containsKey("dimensoes")) {
            Object value = fields.remove("dimensoes");
            fields.put("dimensoesUnitarias", value);
        }
    }
}
