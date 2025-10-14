package com.dcriar.domain.production.service.impl;

import com.dcriar.api.dto.request.production.CorteRealizadoRequestDTO;
import com.dcriar.api.dto.response.production.CorteRealizadoResponseDTO;
import com.dcriar.api.mapper.production.CorteRealizadoMapper;
import com.dcriar.domain.production.entity.CorteRealizado;
import com.dcriar.domain.production.entity.OrdemDeProducao;
import com.dcriar.domain.production.repository.CorteRealizadoRepository;
import com.dcriar.domain.production.repository.OrdemDeProducaoRepository;
import com.dcriar.domain.production.service.CorteRealizadoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementação das operações de negócio para CorteRealizado.
 * <p>
 * Centraliza regras de negócio de criação e atualização utilizando os métodos from e updateFrom da entidade CorteRealizado.
 * Realiza conversão para DTOs de resposta por meio do mapper, garantindo padronização e encapsulamento dos dados.
 * Todos os métodos são transacionais para garantir integridade das operações.
 */
@Service
@RequiredArgsConstructor
public class CorteRealizadoServiceImpl implements CorteRealizadoService {

    private final CorteRealizadoRepository corteRealizadoRepository;
    private final OrdemDeProducaoRepository ordemDeProducaoRepository;
    private final CorteRealizadoMapper corteRealizadoMapper;

    /**
     * Cria um novo registro de CorteRealizado.
     * <p>
     * Busca a ordem de produção associada, utiliza o método from para centralizar regras de negócio e salva o registro.
     *
     * @param dto DTO com os dados do corte realizado
     * @return DTO de resposta do corte realizado cadastrado
     * @throws IllegalArgumentException se a ordem de produção não for encontrada
     */
    @Override
    @Transactional
    public CorteRealizadoResponseDTO create(CorteRealizadoRequestDTO dto) {
        OrdemDeProducao ordem = ordemDeProducaoRepository.findById(dto.getOrdemDeProducaoId())
                .orElseThrow(() -> new IllegalArgumentException("Ordem de produção não encontrada: " + dto.getOrdemDeProducaoId()));
        CorteRealizado corte = CorteRealizado.from(dto, ordem);
        CorteRealizado salvo = corteRealizadoRepository.save(corte);
        return corteRealizadoMapper.toResponseDTO(salvo);
    }

    /**
     * Atualiza um registro existente de CorteRealizado.
     * <p>
     * Busca o corte e a ordem de produção associada, utiliza o método updateFrom para centralizar regras de negócio e salva o registro.
     *
     * @param id  ID do corte realizado
     * @param dto DTO com os dados para atualização
     * @return DTO de resposta do corte realizado atualizado
     * @throws IllegalArgumentException se o corte ou a ordem de produção não forem encontrados
     */
    @Override
    @Transactional
    public CorteRealizadoResponseDTO update(Long id, CorteRealizadoRequestDTO dto) {
        CorteRealizado corte = corteRealizadoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Corte não encontrado: " + id));
        OrdemDeProducao ordem = ordemDeProducaoRepository.findById(dto.getOrdemDeProducaoId())
                .orElseThrow(() -> new IllegalArgumentException("Ordem de produção não encontrada: " + dto.getOrdemDeProducaoId()));
        corte.updateFrom(dto, ordem);
        CorteRealizado atualizado = corteRealizadoRepository.save(corte);
        return corteRealizadoMapper.toResponseDTO(atualizado);
    }

    /**
     * Consulta um corte realizado pelo ID.
     *
     * @param id ID do corte realizado
     * @return DTO de resposta do corte realizado encontrado
     * @throws IllegalArgumentException se o corte não for encontrado
     */
    @Override
    @Transactional
    public CorteRealizadoResponseDTO findById(Long id) {
        CorteRealizado corte = corteRealizadoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Corte não encontrado: " + id));
        return corteRealizadoMapper.toResponseDTO(corte);
    }

    /**
     * Lista todos os cortes realizados de uma ordem de produção.
     *
     * @param ordemDeProducaoId ID da ordem de produção
     * @return Lista de DTOs de resposta dos cortes realizados
     * @throws IllegalArgumentException se a ordem de produção não for encontrada
     */
    @Override
    @Transactional
    public List<CorteRealizadoResponseDTO> findByOrdemDeProducao(Long ordemDeProducaoId) {
        OrdemDeProducao ordem = ordemDeProducaoRepository.findById(ordemDeProducaoId)
                .orElseThrow(() -> new IllegalArgumentException("Ordem de produção não encontrada: " + ordemDeProducaoId));
        return corteRealizadoRepository.findByOrdemDeProducao(ordem)
                .stream()
                .map(corteRealizadoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lista todos os cortes realizados cadastrados no sistema.
     *
     * @return Lista de DTOs de resposta de todos os cortes realizados
     */
    @Override
    @Transactional
    public List<CorteRealizadoResponseDTO> findAll() {
        return corteRealizadoRepository.findAll()
                .stream()
                .map(corteRealizadoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Exclui um corte realizado pelo ID.
     *
     * @param id ID do corte realizado a ser excluído
     */
    @Override
    @Transactional
    public void delete(Long id) {
        corteRealizadoRepository.deleteById(id);
    }
}
