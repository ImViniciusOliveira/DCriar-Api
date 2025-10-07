package com.dcriar.api.mapper.product;

import com.dcriar.api.dto.response.product.CanalEstoqueDTO;
import com.dcriar.domain.product.entity.Estoque;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CanalEstoqueDTOMapper {

    @Mapping(source = "canalVenda.nome", target = "canalNome")
    @Mapping(source = "quantidade", target = "quantidade")
    CanalEstoqueDTO toDto(Estoque estoque);
}
