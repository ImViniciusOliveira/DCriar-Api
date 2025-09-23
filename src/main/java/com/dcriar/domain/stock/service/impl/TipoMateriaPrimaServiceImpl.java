package com.dcriar.domain.stock.service.impl;

import com.dcriar.api.dto.request.stock.TipoMateriaPrimaRequestDTO;
import com.dcriar.api.dto.response.stock.TipoMateriaPrimaResponseDTO;
import com.dcriar.api.mapper.stock.TipoMateriaPrimaMapper;
import com.dcriar.domain.stock.entity.TipoMateriaPrima;
import com.dcriar.domain.stock.repository.TipoMateriaPrimaRepository;
import com.dcriar.domain.stock.service.TipoMateriaPrimaService;
import com.dcriar.exception.custom.TipoMateriaPrimaNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementação da lógica de negócio para gestão de Tipos de Matéria-Prima.
 * <p>
 * Orquestra as operações de CRUD, validações e interações com o repositório,
 * garantindo a integridade dos dados através de transações.
 */
@Service
@RequiredArgsConstructor
public class TipoMateriaPrimaServiceImpl implements TipoMateriaPrimaService {

    private final TipoMateriaPrimaRepository tipoMateriaPrimaRepository;
    private final TipoMateriaPrimaMapper tipoMateriaPrimaMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TipoMateriaPrimaResponseDTO> findAll() {
        return tipoMateriaPrimaRepository.findAll().stream()
                .map(tipoMateriaPrimaMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TipoMateriaPrimaResponseDTO findById(Long id) {
        TipoMateriaPrima tipoMateriaPrima = findTipoById(id);
        return tipoMateriaPrimaMapper.toResponseDTO(tipoMateriaPrima);
    }

    @Override
    @Transactional
    public TipoMateriaPrimaResponseDTO create(TipoMateriaPrimaRequestDTO requestDTO) {
        TipoMateriaPrima tipoMateriaPrima = tipoMateriaPrimaMapper.toEntity(requestDTO);
        TipoMateriaPrima tipoSalvo = tipoMateriaPrimaRepository.save(tipoMateriaPrima);
        return tipoMateriaPrimaMapper.toResponseDTO(tipoSalvo);
    }

    @Override
    @Transactional
    public TipoMateriaPrimaResponseDTO update(Long id, TipoMateriaPrimaRequestDTO requestDTO) {
        TipoMateriaPrima tipoMateriaPrima = findTipoById(id);

        tipoMateriaPrima.setNome(requestDTO.nome());
        tipoMateriaPrima.setUnidadeDeConsumo(requestDTO.unidadeDeConsumo());

        TipoMateriaPrima tipoAtualizado = tipoMateriaPrimaRepository.save(tipoMateriaPrima);
        return tipoMateriaPrimaMapper.toResponseDTO(tipoAtualizado);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!tipoMateriaPrimaRepository.existsById(id)) {
            throw new TipoMateriaPrimaNotFoundException(id);
        }
        tipoMateriaPrimaRepository.deleteById(id);
    }

    /**
     * Método auxiliar para procurar um tipo de matéria-prima por ID, lançando uma exceção padronizada se não for encontrado.
     *
     * @param id O ID do tipo.
     * @return A entidade TipoMateriaPrima encontrada.
     */
    private TipoMateriaPrima findTipoById(Long id) {
        return tipoMateriaPrimaRepository.findById(id)
                .orElseThrow(() -> new TipoMateriaPrimaNotFoundException(id));
    }
}
