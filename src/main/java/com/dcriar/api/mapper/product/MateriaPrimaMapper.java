package com.dcriar.api.mapper.product;

import com.dcriar.api.dto.response.product.MateriaPrimaResponseDTO;
import com.dcriar.domain.stock.entity.TipoMateriaPrima;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MateriaPrimaMapper {
    MateriaPrimaResponseDTO toResponseDTO(TipoMateriaPrima tipoMateriaPrima);
}
