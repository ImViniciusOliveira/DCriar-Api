package com.dcriar.api.mapper.production;

import com.dcriar.api.dto.response.production.OrdemDeCorteResponseDTO;
import com.dcriar.domain.production.entity.OrdemDeCorte;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * Mapper para converter entre a entidade {@link OrdemDeCorte} e o DTO {@link OrdemDeCorteResponseDTO}.
 * Utiliza MapStruct para geração automática de código.
 */
@Mapper(componentModel = "spring")
public interface OrdemDeCorteMapper {

    OrdemDeCorteMapper INSTANCE = Mappers.getMapper(OrdemDeCorteMapper.class);

    /**
     * Converte uma entidade {@link OrdemDeCorte} para um DTO {@link OrdemDeCorteResponseDTO}.
     *
     * @param entity A entidade OrdemDeCorte a ser convertida.
     * @return O DTO OrdemDeCorteResponseDTO resultante.
     */
    @Mapping(source = "produto.id", target = "produtoId")
    @Mapping(source = "larguraFinalCm", target = "tamanhoFinal.larguraCm")
    @Mapping(source = "comprimentoFinalCm", target = "tamanhoFinal.comprimentoCm")
    @Mapping(source = "modoCalculo", target = "modoCalculo")
    @Mapping(source = "canalVendaDestinoId", target = "canalVendaDestinoId")
    // Removendo ignore = true para margens e cortesRealizados
    OrdemDeCorteResponseDTO toDto(OrdemDeCorte entity);

    /**
     * Converte uma lista de entidades {@link OrdemDeCorte} para uma lista de DTOs {@link OrdemDeCorteResponseDTO}.
     *
     * @param entities A lista de entidades OrdemDeCorte a ser convertida.
     * @return A lista de DTOs OrdemDeCorteResponseDTO resultante.
     */
    List<OrdemDeCorteResponseDTO> toDtoList(List<OrdemDeCorte> entities);
}
