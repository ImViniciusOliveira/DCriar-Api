package com.dcriar.api.mapper.product;

import com.dcriar.api.dto.response.product.DimensoesResponseDTO;
import com.dcriar.domain.product.entity.Dimensoes;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * Mapper para converter a entidade {@link Dimensoes} em seu DTO de resposta.
 * Utiliza MapStruct para facilitar o mapeamento entre os campos.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DimensoesMapper {
    /**
     * Converte a entidade Dimensoes para DimensoesResponseDTO.
     *
     * @param dimensoes entidade de origem
     * @return DTO de resposta
     */
    @Mapping(source = "larguraCm", target = "largura")
    @Mapping(source = "comprimentoCm", target = "comprimento")
    DimensoesResponseDTO toResponseDTO(Dimensoes dimensoes);
}
