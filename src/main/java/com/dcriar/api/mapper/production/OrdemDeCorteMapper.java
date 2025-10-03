package com.dcriar.api.mapper.production;

import com.dcriar.api.dto.request.production.MargensRequestDTO;
import com.dcriar.api.dto.response.production.CorteRealizadoDTO;
import com.dcriar.api.dto.response.production.OrdemDeCorteResponseDTO;
import com.dcriar.domain.production.entity.CorteRealizado;
import com.dcriar.domain.production.entity.Margens;
import com.dcriar.domain.production.entity.OrdemDeCorte;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrdemDeCorteMapper {

    OrdemDeCorteMapper INSTANCE = Mappers.getMapper(OrdemDeCorteMapper.class);

    @Mapping(source = "produto.id", target = "produtoId")
    @Mapping(source = "larguraFinalCm", target = "tamanhoFinal.larguraCm")
    @Mapping(source = "comprimentoFinalCm", target = "tamanhoFinal.comprimentoCm")
    @Mapping(source = "modoCalculo", target = "modoCalculo")
    @Mapping(source = "canalVendaDestinoId", target = "canalVendaDestinoId")
    @Mapping(source = "margens", target = "margens") // Mapeia a entidade Margens para o DTO
    @Mapping(source = "cortesRealizados", target = "cortesRealizados") // Mapeia a lista de entidades CorteRealizado
    OrdemDeCorteResponseDTO toDto(OrdemDeCorte entity);

    List<OrdemDeCorteResponseDTO> toDtoList(List<OrdemDeCorte> entities);

    // Conversores para os tipos aninhados
    MargensRequestDTO toMargensDto(Margens margens);

    Margens toMargensEntity(MargensRequestDTO margensRequestDTO);

    CorteRealizadoDTO toCorteRealizadoDto(CorteRealizado corteRealizado);

    List<CorteRealizadoDTO> toCorteRealizadoDtoList(List<CorteRealizado> cortesRealizados);
}
