package com.dcriar.api.mapper.product;

import com.dcriar.api.dto.response.product.DimensoesResponseDTO;
import com.dcriar.domain.product.entity.Dimensoes;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DimensoesMapper {
    @Mapping(source = "larguraCm", target = "largura")
    @Mapping(source = "comprimentoCm", target = "comprimento")
    DimensoesResponseDTO toResponseDTO(Dimensoes dimensoes);
}
