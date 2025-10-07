package com.dcriar.api.mapper.product;

import com.dcriar.api.dto.response.product.ProdutoEstoqueDTO;
import com.dcriar.domain.product.entity.Estoque;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = CanalEstoqueDTOMapper.class)
public interface ProdutoEstoqueDTOMapper {

    @Mapping(source = "produtoId", target = "produtoId")
    @Mapping(source = "estoques", target = "canais")
    ProdutoEstoqueDTO toDto(Long produtoId, List<Estoque> estoques);
}
