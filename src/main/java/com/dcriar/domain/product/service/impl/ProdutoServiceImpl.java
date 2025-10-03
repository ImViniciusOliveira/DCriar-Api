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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementação da lógica de negócio para gerenciamento de Produtos ("moldes").
 * <p>
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

    /**
     * Busca todos os produtos cadastrados no sistema.
     * Para cada produto, calcula e enriquece o DTO de resposta com informações de estoque.
     *
     * @return Uma lista de {@link ProdutoResponseDTO} contendo todos os produtos com seus saldos de estoque.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProdutoResponseDTO> findAll() {
        return produtoRepository.findAll().stream()
                .map(this::mapAndEnrichProduto)
                .collect(Collectors.toList());
    }

    /**
     * Busca um produto específico pelo seu ID.
     * Calcula e enriquece o DTO de resposta com informações de estoque.
     *
     * @param id O ID do produto a ser buscado.
     * @return O {@link ProdutoResponseDTO} do produto encontrado com seus saldos de estoque.
     * @throws ProdutoNotFoundException se o produto com o ID especificado não for encontrado.
     */
    @Override
    @Transactional(readOnly = true)
    public ProdutoResponseDTO findById(Long id) {
        Produto produto = findProdutoById(id);
        return mapAndEnrichProduto(produto);
    }

    /**
     * Cria um novo produto no sistema.
     * <p>
     * Realiza validações de negócio para garantir a unicidade do nome e SKU.
     * Associa o produto a um tipo de matéria-prima existente.
     *
     * @param requestDTO O DTO com os dados para a criação do produto.
     * @return O {@link ProdutoResponseDTO} do produto recém-criado com seus saldos de estoque.
     * @throws ProdutoInvalidoException se houver erros de validação de negócio (nome/SKU duplicados).
     * @throws TipoMateriaPrimaNotFoundException se o tipo de matéria-prima especificado não for encontrado.
     */
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

    /**
     * Atualiza um produto existente pelo seu ID.
     * <p>
     * Realiza validações de negócio para garantir a unicidade do nome e SKU, excluindo o próprio produto.
     * Associa o produto a um tipo de matéria-prima existente.
     *
     * @param id O ID do produto a ser atualizado.
     * @param requestDTO O DTO com os novos dados para atualização.
     * @return O {@link ProdutoResponseDTO} do produto atualizado com seus saldos de estoque.
     * @throws ProdutoNotFoundException se o produto com o ID especificado não for encontrado.
     * @throws ProdutoInvalidoException se houver erros de validação de negócio (nome/SKU duplicados).
     * @throws TipoMateriaPrimaNotFoundException se o tipo de matéria-prima especificado não for encontrado.
     */
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

    /**
     * Deleta um produto pelo seu ID.
     * <p>
     * Antes de deletar, verifica se o produto não está em uso em nenhuma ordem de corte.
     *
     * @param id O ID do produto a ser deletado.
     * @throws ProdutoNotFoundException se o produto com o ID especificado não for encontrado.
     * @throws ProdutoEmUsoException se o produto estiver em uso em uma ou mais ordens de corte.
     */
    @Override
    @Transactional
    public void deleteById(Long id) {
        Produto produto = findProdutoById(id);

        List<com.dcriar.domain.production.entity.OrdemDeCorte> ordens = ordemDeCorteRepository.findAllByProduto(produto);
        if (!ordens.isEmpty()) {
            Set<Long> ordemIds = ordens.stream().map(com.dcriar.domain.production.entity.OrdemDeCorte::getId).collect(Collectors.toSet());
            throw new ProdutoEmUsoException(id, ordemIds);
        }

        produtoRepository.delete(produto);
    }

    /**
     * Mapeia uma entidade {@link Produto} para um {@link ProdutoResponseDTO} e enriquece
     * o DTO com informações de estoque calculadas dinamicamente.
     * <p>
     * Calcula o estoque físico total, o estoque distribuído entre os canais e o estoque
     * disponível para alocação.
     *
     * @param produto A entidade {@link Produto} a ser mapeada e enriquecida.
     * @return Um {@link ProdutoResponseDTO} com os dados do produto e informações de estoque.
     */
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

    /**
     * Busca uma entidade {@link Produto} pelo seu ID.
     * Método auxiliar para evitar duplicação de código e centralizar o tratamento de "não encontrado".
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
     * Valida as regras de negócio para a criação ou atualização de um produto.
     * <p>
     * Verifica a unicidade do nome e do SKU, considerando se é uma operação de criação ou atualização.
     *
     * @param requestDTO O DTO de requisição do produto.
     * @param produtoId O ID do produto, se for uma operação de atualização (nulo para criação).
     * @throws ProdutoInvalidoException se houver erros de validação de negócio.
     */
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
