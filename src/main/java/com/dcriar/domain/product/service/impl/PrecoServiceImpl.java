package com.dcriar.domain.product.service.impl;

import com.dcriar.api.dto.request.product.PrecoRequestDTO;
import com.dcriar.api.dto.response.product.PrecoResponseDTO;
import com.dcriar.domain.product.entity.Preco;
import com.dcriar.domain.product.entity.Produto;
import com.dcriar.domain.product.repository.PrecoRepository;
import com.dcriar.domain.product.repository.ProdutoRepository;
import com.dcriar.domain.product.service.PrecoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementação das operações de negócio para Preco.
 * <p>
 * Utiliza os métodos from e updateFrom da entidade Preco para centralizar regras de negócio de criação e atualização.
 * Realiza conversão para DTOs de resposta, garantindo padronização e encapsulamento dos dados.
 */
@Service
@RequiredArgsConstructor
public class PrecoServiceImpl implements PrecoService {

    private final PrecoRepository precoRepository;
    private final ProdutoRepository produtoRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public PrecoResponseDTO create(Long produtoId, PrecoRequestDTO dto) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + produtoId));
        Preco preco = Preco.from(dto, produto);
        Preco salvo = precoRepository.save(preco);
        return toResponseDTO(salvo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public PrecoResponseDTO update(Long precoId, PrecoRequestDTO dto) {
        Preco preco = precoRepository.findById(precoId)
                .orElseThrow(() -> new IllegalArgumentException("Preço não encontrado: " + precoId));
        Produto produto = produtoRepository.findById(dto.getProdutoId())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + dto.getProdutoId()));
        preco.updateFrom(dto, produto);
        Preco atualizado = precoRepository.save(preco);
        return toResponseDTO(atualizado);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public PrecoResponseDTO findById(Long precoId) {
        Preco preco = precoRepository.findById(precoId)
                .orElseThrow(() -> new IllegalArgumentException("Preço não encontrado: " + precoId));
        return toResponseDTO(preco);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public List<PrecoResponseDTO> findByProduto(Long produtoId) {
        Produto produto = produtoRepository.findById(produtoId)
            .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + produtoId));
        return precoRepository.findByProduto(produto)
            .stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public List<PrecoResponseDTO> findAll() {
        return precoRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void delete(Long precoId) {
        precoRepository.deleteById(precoId);
    }

    /**
     * Converte a entidade Preco para o DTO de resposta.
     * <p>
     * Realiza apenas mapeamento simples dos campos, sem lógica de negócio.
     *
     * @param preco Entidade Preco
     * @return DTO de resposta
     */
    private PrecoResponseDTO toResponseDTO(Preco preco) {
        return PrecoResponseDTO.builder()
                .id(preco.getId())
                .produtoId(preco.getProduto().getId())
                .tipoPreco(preco.getTipoPreco().name())
                .valor(preco.getValor())
                .valorPromocional(preco.getValorPromocional())
                .promocaoAtiva(preco.isPromocaoAtiva())
                .build();
    }
}
