package com.dcriar.domain.product.service.impl;

import com.dcriar.api.dto.request.product.CanalVendaRequestDTO;
import com.dcriar.api.dto.response.product.CanalVendaResponseDTO;
import com.dcriar.domain.product.entity.CanalVenda;
import com.dcriar.domain.product.repository.CanalVendaRepository;
import com.dcriar.domain.product.service.CanalVendaService;
import com.dcriar.exception.custom.CanalVendaNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementação da lógica de negócio para CanalVenda.
 * <p>
 * Utiliza os métodos from() e updateFrom() da entidade para centralizar regras de negócio de criação e atualização.
 */
@Service
@RequiredArgsConstructor
public class CanalVendaServiceImpl implements CanalVendaService {

    private final CanalVendaRepository canalVendaRepository;

    /**
     * Cria um novo canal de venda.
     * Utiliza o método {@link CanalVenda#from} para centralizar regras de negócio de criação.
     *
     * @param requestDTO DTO de request com os dados do canal de venda
     * @return DTO de resposta do canal criado
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
     * Utiliza o método {@link CanalVenda#updateFrom} para centralizar regras de negócio de atualização.
     *
     * @param id          ID do canal de venda
     * @param requestDTO  DTO de request com os dados para atualização
     * @return DTO de resposta do canal atualizado
     */
    @Override
    @Transactional
    public CanalVendaResponseDTO update(Long id, CanalVendaRequestDTO requestDTO) {
        CanalVenda canal = canalVendaRepository.findById(id)
                .orElseThrow(() -> new CanalVendaNotFoundException(id));
        canal.updateFrom(requestDTO);
        CanalVenda atualizado = canalVendaRepository.save(canal);
        return toResponseDTO(atualizado);
    }

    /**
     * Busca um canal de venda pelo ID.
     *
     * @param id ID do canal de venda
     * @return DTO de resposta do canal encontrado
     */
    @Override
    @Transactional(readOnly = true)
    public CanalVendaResponseDTO findById(Long id) {
        CanalVenda canal = canalVendaRepository.findById(id)
                .orElseThrow(() -> new CanalVendaNotFoundException(id));
        return toResponseDTO(canal);
    }

    /**
     * Lista todos os canais de venda.
     *
     * @return Lista de DTOs de resposta
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
     * @param canal Entidade CanalVenda
     * @return DTO de resposta
     */
    private CanalVendaResponseDTO toResponseDTO(CanalVenda canal) {
        return CanalVendaResponseDTO.builder()
                .id(canal.getId())
                .nome(canal.getNome())
                .build();
    }
}
