package com.dcriar.api.mapper.product;

import com.dcriar.api.dto.response.product.DimensoesResponseDTO;
import com.dcriar.domain.product.entity.Dimensoes;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * Mapper para converter a entidade {@link Dimensoes} em seu DTO de resposta.
 * <p>
 * Como os nomes dos campos na entidade e no DTO são idênticos (ex: larguraCm),
 * o MapStruct realiza o mapeamento automaticamente sem a necessidade de anotações @Mapping.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DimensoesMapper {

    /**
     * Converte a entidade Dimensoes para DimensoesResponseDTO.
     *
     * @param dimensoes entidade de origem
     * @return DTO de resposta
     */
    DimensoesResponseDTO toResponseDTO(Dimensoes dimensoes);
}