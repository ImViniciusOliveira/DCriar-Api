package com.dcriar.domain.product.service.impl;

import com.dcriar.api.dto.request.product.CanalVendaRequestDTO;
import com.dcriar.api.dto.response.product.CanalVendaResponseDTO;
import com.dcriar.domain.product.entity.CanalVenda;
import com.dcriar.domain.product.repository.CanalVendaRepository;
import com.dcriar.domain.product.service.CanalVendaService;
import com.dcriar.exception.custom.CanalVendaNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementação da lógica de negócio para CanalVenda.
 * <p>
 * As regras de negócio, como a validação de nome único, são centralizadas
 * nos métodos de fábrica ({@code from}) e de atualização ({@code updateFrom}) da entidade {@link CanalVenda}.
 */
@Service
@RequiredArgsConstructor
public class CanalVendaServiceImpl implements CanalVendaService {

    private final CanalVendaRepository canalVendaRepository;

    /**
     * Cria um novo canal de venda.
     * <p>
     * <b>Regras de negócio:</b>
     * <ul>
     *     <li>O nome do canal de venda não pode ser duplicado.</li>
     * </ul>
     * A lógica de validação e criação é delegada ao método {@link CanalVenda#from(CanalVendaRequestDTO)}.
     *
     * @param requestDTO DTO de request com os dados do canal de venda.
     * @return DTO de resposta do canal criado.
     */
    @Override
    @Transactional
    public CanalVendaResponseDTO create(CanalVendaRequestDTO requestDTO) {
        CanalVenda canal = CanalVenda.from(requestDTO);
        CanalVenda salvo = canalVendaRepository.save(canal);
        return toResponseDTO(salvo);
    }

    /**
     * Atualiza um canal de venda existente.
     * <p>
     * <b>Regras de negócio:</b>
     * <ul>
     *     <li>O novo nome do canal de venda não pode ser duplicado.</li>
     * </ul>
     * A lógica de validação e atualização é delegada ao método {@link CanalVenda#updateFrom(CanalVendaRequestDTO)}.
     *
     * @param id          ID do canal de venda a ser atualizado.
     * @param requestDTO  DTO de request com os dados para atualização.
     * @return DTO de resposta do canal atualizado.
     * @throws CanalVendaNaoEncontradoException se o canal de venda não for encontrado.
     */
    @Override
    @Transactional
    public CanalVendaResponseDTO update(Long id, CanalVendaRequestDTO requestDTO) {
        CanalVenda canal = canalVendaRepository.findById(id)
                .orElseThrow(() -> new CanalVendaNaoEncontradoException(id));
        canal.updateFrom(requestDTO);
        CanalVenda atualizado = canalVendaRepository.save(canal);
        return toResponseDTO(atualizado);
    }

    /**
     * Busca um canal de venda pelo ID.
     *
     * @param id ID do canal de venda.
     * @return DTO de resposta do canal encontrado.
     * @throws CanalVendaNaoEncontradoException se o canal de venda não for encontrado.
     */
    @Override
    @Transactional(readOnly = true)
    public CanalVendaResponseDTO findById(Long id) {
        CanalVenda canal = canalVendaRepository.findById(id)
                .orElseThrow(() -> new CanalVendaNaoEncontradoException(id));
        return toResponseDTO(canal);
    }

    /**
     * Lista todos os canais de venda.
     *
     * @return Lista de DTOs de resposta.
     */
    @Override
    @Transactional(readOnly = true)
    public List<CanalVendaResponseDTO> findAll() {
        return canalVendaRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converte a entidade CanalVenda para o DTO de resposta.
     * <p>
     * Conversão simples, sem lógica de negócio.
     *
     * @param canal Entidade CanalVenda.
     * @return DTO de resposta.
     */
    private CanalVendaResponseDTO toResponseDTO(CanalVenda canal) {
        return CanalVendaResponseDTO.builder()
                .id(canal.getId())
                .nome(canal.getNome())
                .build();
    }
}
