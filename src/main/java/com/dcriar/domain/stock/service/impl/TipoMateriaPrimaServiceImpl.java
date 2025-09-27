package com.dcriar.domain.stock.service.impl;

import com.dcriar.api.dto.request.stock.TipoMateriaPrimaRequestDTO;
import com.dcriar.api.dto.response.stock.TipoMateriaPrimaResponseDTO;
import com.dcriar.api.mapper.stock.TipoMateriaPrimaMapper;
import com.dcriar.domain.stock.entity.LoteMateriaPrima;
import com.dcriar.domain.stock.entity.TipoMateriaPrima;
import com.dcriar.domain.stock.repository.LoteMateriaPrimaRepository;
import com.dcriar.domain.stock.repository.TipoMateriaPrimaRepository;
import com.dcriar.domain.stock.service.TipoMateriaPrimaService;
import com.dcriar.exception.custom.TipoMateriaPrimaAlreadyExistsException;
import com.dcriar.exception.custom.TipoMateriaPrimaEmUsoException;
import com.dcriar.exception.custom.TipoMateriaPrimaNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TipoMateriaPrimaServiceImpl implements TipoMateriaPrimaService {

    private final TipoMateriaPrimaRepository tipoMateriaPrimaRepository;
    private final TipoMateriaPrimaMapper tipoMateriaPrimaMapper;
    private final LoteMateriaPrimaRepository loteMateriaPrimaRepository;

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
        TipoMateriaPrima tipo = findTipoById(id);
        return tipoMateriaPrimaMapper.toResponseDTO(tipo);
    }

    @Override
    @Transactional
    public TipoMateriaPrimaResponseDTO create(TipoMateriaPrimaRequestDTO requestDTO) {
        validateNomeDisponivel(requestDTO.nome());

        TipoMateriaPrima tipo = TipoMateriaPrima.builder()
                .nome(requestDTO.nome())
                .unidadeDeConsumo(requestDTO.unidadeDeConsumo())
                .build();

        TipoMateriaPrima salvo = tipoMateriaPrimaRepository.save(tipo);
        return tipoMateriaPrimaMapper.toResponseDTO(salvo);
    }

    @Override
    @Transactional
    public TipoMateriaPrimaResponseDTO update(Long id, TipoMateriaPrimaRequestDTO requestDTO) {
        TipoMateriaPrima tipo = findTipoById(id);

        if (requestDTO.nome() != null && !tipo.getNome().equalsIgnoreCase(requestDTO.nome())) {
            validateNomeDisponivel(requestDTO.nome());
            tipo.setNome(requestDTO.nome());
        }

        if (requestDTO.unidadeDeConsumo() != null) {
            tipo.setUnidadeDeConsumo(requestDTO.unidadeDeConsumo());
        }

        TipoMateriaPrima atualizado = tipoMateriaPrimaRepository.save(tipo);
        return tipoMateriaPrimaMapper.toResponseDTO(atualizado);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        TipoMateriaPrima tipo = findTipoById(id);

        // Busca todos os lotes que usam esse tipo de matéria-prima
        List<LoteMateriaPrima> lotes = loteMateriaPrimaRepository.findAllByTipoMateriaPrima(tipo);
        if (!lotes.isEmpty()) {
            Set<Long> loteIds = lotes.stream().map(LoteMateriaPrima::getId).collect(Collectors.toSet());
            throw new TipoMateriaPrimaEmUsoException(id, loteIds);
        }

        tipoMateriaPrimaRepository.delete(tipo);
    }

    /* ==========================
       Métodos auxiliares privados
       ========================== */

    private TipoMateriaPrima findTipoById(Long id) {
        return tipoMateriaPrimaRepository.findById(id)
                .orElseThrow(() -> new TipoMateriaPrimaNotFoundException(id));
    }

    private void validateNomeDisponivel(String nome) {
        if (tipoMateriaPrimaRepository.existsByNome(nome)) {
            throw new TipoMateriaPrimaAlreadyExistsException(nome);
        }
    }
}
