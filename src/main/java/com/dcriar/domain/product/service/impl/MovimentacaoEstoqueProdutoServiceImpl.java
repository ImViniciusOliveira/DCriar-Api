package com.dcriar.domain.product.service.impl;

import com.dcriar.api.dto.request.product.MovimentacaoEstoqueProdutoRequestDTO;
import com.dcriar.api.dto.response.product.MovimentacaoEstoqueProdutoResponseDTO;
import com.dcriar.domain.product.entity.MovimentacaoEstoqueProduto;
import com.dcriar.domain.product.entity.Produto;
import com.dcriar.domain.product.repository.MovimentacaoEstoqueProdutoRepository;
import com.dcriar.domain.product.repository.ProdutoRepository;
import com.dcriar.domain.product.service.MovimentacaoEstoqueProdutoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service para manipulação de movimentações de estoque de produto acabado.
 * <p>
 * Centraliza regras de negócio e uso dos métodos from/updateFrom da entidade.
 */
@Service
@RequiredArgsConstructor
public class MovimentacaoEstoqueProdutoServiceImpl implements MovimentacaoEstoqueProdutoService {

    private final MovimentacaoEstoqueProdutoRepository movimentacaoRepository;
    private final ProdutoRepository produtoRepository;

    /**
     * Registra uma nova movimentação de estoque para um produto.
     * <p>
     * Utiliza o método from da entidade para centralizar regras de negócio.
     *
     * @param requestDTO DTO de request com os dados da movimentação
     * @return DTO de resposta da movimentação registrada
     */
    @Transactional
    public MovimentacaoEstoqueProdutoResponseDTO registrarMovimentacao(MovimentacaoEstoqueProdutoRequestDTO requestDTO) {
        Produto produto = produtoRepository.findById(requestDTO.getProdutoId())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + requestDTO.getProdutoId()));
        MovimentacaoEstoqueProduto movimentacao = MovimentacaoEstoqueProduto.from(requestDTO, produto);
        movimentacaoRepository.save(movimentacao);
        return toResponseDTO(movimentacao);
    }

    /**
     * Atualiza uma movimentação existente.
     * <p>
     * Utiliza o método updateFrom da entidade para centralizar regras de negócio.
     *
     * @param id ID da movimentação
     * @param requestDTO DTO de request com os dados atualizados
     * @return DTO de resposta da movimentação atualizada
     */
    @Transactional
    public MovimentacaoEstoqueProdutoResponseDTO atualizarMovimentacao(Long id, MovimentacaoEstoqueProdutoRequestDTO requestDTO) {
        MovimentacaoEstoqueProduto movimentacao = movimentacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Movimentação não encontrada: " + id));
        Produto produto = produtoRepository.findById(requestDTO.getProdutoId())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + requestDTO.getProdutoId()));
        movimentacao.updateFrom(requestDTO, produto);
        movimentacaoRepository.save(movimentacao);
        return toResponseDTO(movimentacao);
    }

    /**
     * Lista todas as movimentações de estoque de um produto.
     *
     * @param produtoId ID do produto
     * @return Lista de DTOs de resposta das movimentações
     */
    public List<MovimentacaoEstoqueProdutoResponseDTO> listarPorProduto(Long produtoId) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + produtoId));
        return movimentacaoRepository.findAllByProduto(produto).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converte a entidade MovimentacaoEstoqueProduto para o DTO de resposta.
     * <p>
     * Conversão simples, sem lógica de negócio.
     *
     * @param movimentacao Entidade movimentação
     * @return DTO de resposta
     */
    private MovimentacaoEstoqueProdutoResponseDTO toResponseDTO(MovimentacaoEstoqueProduto movimentacao) {
        return MovimentacaoEstoqueProdutoResponseDTO.builder()
                .id(movimentacao.getId())
                .produtoId(movimentacao.getProduto().getId())
                .data(movimentacao.getData())
                .tipo(movimentacao.getTipo().name())
                .quantidade(movimentacao.getQuantidade())
                .motivo(movimentacao.getMotivo())
                .build();
    }
}
