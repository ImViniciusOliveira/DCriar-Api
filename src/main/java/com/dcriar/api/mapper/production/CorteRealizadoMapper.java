package com.dcriar.api.mapper.production;

import com.dcriar.api.dto.response.production.CorteRealizadoResponseDTO;
import com.dcriar.domain.production.entity.CorteRealizado;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * Mapper para conversão simples entre CorteRealizado e CorteRealizadoResponseDTO.
 * <p>
 * Não deve conter lógica de negócio, apenas mapeamento de campos.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CorteRealizadoMapper {
    /**
     * Converte a entidade CorteRealizado para o DTO de resposta.
     * Mapeia explicitamente o campo ordemDeProducaoId a partir da entidade associada.
     *
     * @param entity Entidade CorteRealizado
     * @return DTO de resposta
     */
    @Mapping(source = "ordemDeProducao.id", target = "ordemDeProducaoId")
    CorteRealizadoResponseDTO toResponseDTO(CorteRealizado entity);
}