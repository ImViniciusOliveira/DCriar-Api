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
import com.dcriar.exception.custom.ProdutoNomeDuplicadoException;
import com.dcriar.exception.custom.ProdutoNaoEncontradoException;
import com.dcriar.exception.custom.ProdutoSkuDuplicadoException;
import com.dcriar.exception.custom.TipoMateriaPrimaNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
     * Cria um novo produto.
     * <p>
     * <b>Regras de Negócio:</b>
     * <ul>
     *     <li>O nome e o SKU do produto devem ser únicos.</li>
     * </ul>
     * <b>Gerenciamento de Imagem:</b>
     * <ul>
     *     <li>A URL da foto fornecida no DTO é processada para extrair e armazenar apenas o nome do arquivo.</li>
     * </ul>
     *
     * @param requestDTO O DTO com os dados do novo produto.
     * @return O DTO de resposta do produto criado, enriquecido com dados de estoque e URL da foto.
     * @throws TipoMateriaPrimaNaoEncontradoException se o tipo de matéria-prima especificado não for encontrado.
     * @throws ProdutoNomeDuplicadoException se já existir um produto com o mesmo nome.
     * @throws ProdutoSkuDuplicadoException se já existir um produto com o mesmo SKU.
     */
    @Override
    @Transactional
    public ProdutoResponseDTO create(ProdutoRequestDTO requestDTO) {
        // 1. Valida regras de negócio de unicidade para nome e SKU.
        validarRegrasDeNegocio(requestDTO, null);

        // 2. Associa o tipo de matéria-prima.
        var tipoMateriaPrima = tipoMateriaPrimaRepository.findById(requestDTO.getTipoMateriaPrimaId())
                .orElseThrow(() -> new TipoMateriaPrimaNaoEncontradoException(requestDTO.getTipoMateriaPrimaId()));

        Produto produto = Produto.from(requestDTO);
        produto.setTipoMateriaPrima(tipoMateriaPrima);

        // 3. Processa a URL da imagem, armazenando apenas o nome do arquivo.
        if (produto.getFotoPrincipalUrl() != null && !produto.getFotoPrincipalUrl().isBlank()) {
            String fileName = fileStorageService.extractFileName(produto.getFotoPrincipalUrl());
            produto.setFotoPrincipalUrl(fileName);
        }

        // 4. Salva o produto no banco de dados.
        Produto produtoSalvo = produtoRepository.save(produto);

        // 5. Retorna o DTO enriquecido.
        return findById(produtoSalvo.getId());
    }

    /**
     * Atualiza um produto existente.
     * <p>
     * <b>Regras de Negócio:</b>
     * <ul>
     *     <li>O nome e o SKU devem ser únicos, ignorando o próprio produto que está sendo atualizado.</li>
     * </ul>
     * <b>Gerenciamento de Imagem:</b>
     * <ul>
     *     <li>Se uma nova URL de foto for fornecida, o arquivo antigo é excluído do armazenamento.</li>
     *     <li>Se a URL da foto for removida (nula ou em branco), o arquivo antigo também é excluído.</li>
     * </ul>
     *
     * @param id O ID do produto a ser atualizado.
     * @param requestDTO O DTO com os dados do produto.
     * @return O DTO de resposta do produto atualizado, enriquecido com dados de estoque e URL da foto.
     * @throws ProdutoNaoEncontradoException se o produto com o ID fornecido não for encontrado.
     * @throws TipoMateriaPrimaNaoEncontradoException se o tipo de matéria-prima especificado não for encontrado.
     * @throws ProdutoNomeDuplicadoException se já existir outro produto com o mesmo nome.
     * @throws ProdutoSkuDuplicadoException se já existir outro produto com o mesmo SKU.
     */
    @Override
    @Transactional
    public ProdutoResponseDTO update(Long id, ProdutoRequestDTO requestDTO) {
        // 1. Busca o produto e armazena o nome do arquivo da foto antiga.
        Produto produto = findProdutoById(id);
        String oldFotoFileName = produto.getFotoPrincipalUrl();

        // 2. Valida regras de negócio de unicidade para nome e SKU.
        validarRegrasDeNegocio(requestDTO, id);

        // 3. Associa o tipo de matéria-prima.
        var tipoMateriaPrima = tipoMateriaPrimaRepository.findById(requestDTO.getTipoMateriaPrimaId())
                .orElseThrow(() -> new TipoMateriaPrimaNaoEncontradoException(requestDTO.getTipoMateriaPrimaId()));

        produto.updateFrom(requestDTO);
        produto.setTipoMateriaPrima(tipoMateriaPrima);

        // 4. Gerencia o ciclo de vida do arquivo de imagem.
        String newFotoUrlFromDto = requestDTO.getFotoPrincipalUrl();

        if (newFotoUrlFromDto != null && !newFotoUrlFromDto.isBlank()) {
            // Se uma nova foto foi enviada, extrai o nome do arquivo.
            String newFileName = fileStorageService.extractFileName(newFotoUrlFromDto);

            // Se a foto antiga existia e é diferente da nova, exclui a antiga.
            if (oldFotoFileName != null && !oldFotoFileName.equals(newFileName)) {
                fileStorageService.deleteFile(oldFotoFileName);
            }
            produto.setFotoPrincipalUrl(newFileName);
        } else {
            // Se a URL da foto foi removida, exclui o arquivo antigo se ele existir.
            if (oldFotoFileName != null && !oldFotoFileName.isBlank()) {
                fileStorageService.deleteFile(oldFotoFileName);
            }
            produto.setFotoPrincipalUrl(null);
        }

        // 5. Salva as alterações.
        produtoRepository.save(produto);

        // 6. Retorna o DTO enriquecido.
        return findById(id);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Atenção:</b> A lógica para atualização parcial (PATCH) ainda não foi implementada.
     * O método atualmente retorna {@code null} e não deve ser utilizado.
     *
     * @param id O ID do produto a ser atualizado.
     * @param fields Um mapa contendo os nomes dos campos e seus novos valores.
     * @return Atualmente {@code null}.
     */
    @Override
    @Transactional
    public ProdutoResponseDTO patch(Long id, Map<String, Object> fields) {
        // TODO: Implementar a lógica de atualização parcial (PATCH).
        // A implementação deve buscar o produto, validar os campos do mapa,
        // aplicar as alterações e salvar o produto. O uso de uma biblioteca como
        // Jackson para mesclar os dados pode ser considerado.
        return null;
    }

    /**
     * Exclui um produto pelo seu ID.
     * <p>
     * <b>Regras de Negócio:</b>
     * <ul>
     *     <li>A exclusão é impedida se o produto estiver vinculado a uma ou mais Ordens de Produção.</li>
     *     <li>Se o produto tiver uma foto associada, o arquivo correspondente é excluído do armazenamento.</li>
     * </ul>
     *
     * @param id O ID do produto a ser excluído.
     * @throws ProdutoNaoEncontradoException se o produto com o ID fornecido não for encontrado.
     * @throws ProdutoEmUsoException se o produto estiver vinculado a uma ou mais ordens de produção.
     */
    @Override
    @Transactional
    public void deleteById(Long id) {
        Produto produto = findProdutoById(id);

        // 1. Verifica se o produto está em uso em Ordens de Produção.
        List<OrdemDeProducao> ordens = ordemDeProducaoRepository.findAllByProduto(produto);
        if (!ordens.isEmpty()) {
            Set<Long> ordemIds = ordens.stream().map(OrdemDeProducao::getId).collect(Collectors.toSet());
            throw new ProdutoEmUsoException(id, ordemIds);
        }

        // 2. Se houver uma foto associada, exclui o arquivo.
        if (produto.getFotoPrincipalUrl() != null && !produto.getFotoPrincipalUrl().isBlank()) {
            fileStorageService.deleteFile(produto.getFotoPrincipalUrl());
        }

        // 3. Exclui o produto do banco de dados.
        produtoRepository.delete(produto);
    }

    /**
     * Mapeia uma entidade {@link Produto} para seu {@link ProdutoResponseDTO} e o enriquece com dados adicionais.
     * <p>
     * Este método realiza as seguintes ações de enriquecimento:
     * <ol>
     *     <li><b>URL da Foto:</b> Constrói a URL de download completa para a foto do produto, se houver.</li>
     *     <li><b>Dados de Estoque:</b> Calcula e adiciona os seguintes campos ao DTO:
     *         <ul>
     *             <li>{@code estoqueFisicoTotal}: O saldo total do produto, com base em todas as movimentações (entradas de produção, saídas de venda, ajustes).</li>
     *             <li>{@code estoqueDistribuidoTotal}: A soma das quantidades do produto que estão alocadas nos estoques dos canais de venda.</li>
     *             <li>{@code estoqueDisponivelParaAlocar}: A quantidade de produto do estoque físico que ainda não foi distribuída para nenhum canal (calculado como {@code estoqueFisicoTotal - estoqueDistribuidoTotal}).</li>
     *         </ul>
     *     </li>
     * </ol>
     *
     * @param produto A entidade {@link Produto} a ser processada.
     * @return O {@link ProdutoResponseDTO} enriquecido.
     */
    private ProdutoResponseDTO mapAndEnrichProduto(Produto produto) {
        ProdutoResponseDTO dto = produtoMapper.toResponseDTO(produto);

        // 1. Constrói a URL completa da foto a partir do nome do arquivo armazenado.
        if (dto.getFotoPrincipalUrl() != null && !dto.getFotoPrincipalUrl().isBlank()) {
            String fileName = dto.getFotoPrincipalUrl();
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/v1/uploads/")
                    .path(fileName)
                    .toUriString();
            dto.setFotoPrincipalUrl(fileDownloadUri);
        }

        // 2. Calcula e define os dados de estoque.
        // Saldo total do produto no estoque mestre.
        Integer estoqueFisicoTotal = movimentacaoEstoqueProdutoRepository.findSaldoByProduto(produto);
        // Soma das quantidades alocadas nos canais de venda.
        List<Estoque> estoquesAtuais = estoqueRepository.findAllByProduto(produto);
        int estoqueDistribuidoTotal = estoquesAtuais.stream()
                .mapToInt(Estoque::getQuantidade)
                .sum();
        // Saldo do estoque mestre que ainda pode ser distribuído.
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
        // Validação para criação de um novo produto.
        if (produtoId == null) {
            if (produtoRepository.existsByNome(requestDTO.getNome())) {
                throw new ProdutoNomeDuplicadoException(requestDTO.getNome());
            }
            if (produtoRepository.existsBySku(requestDTO.getSku())) {
                throw new ProdutoSkuDuplicadoException(requestDTO.getSku());
            }
        } else {
            // Validação para atualização de um produto existente.
            if (produtoRepository.existsByNomeAndIdNot(requestDTO.getNome(), produtoId)) {
                throw new ProdutoNomeDuplicadoException(requestDTO.getNome());
            }
            if (produtoRepository.existsBySkuAndIdNot(requestDTO.getSku(), produtoId)) {
                throw new ProdutoSkuDuplicadoException(requestDTO.getSku());
            }
        }
    }
}
