package com.dcriar.api.mapper.product;

import com.dcriar.api.dto.response.product.ProdutoResponseDTO;
import com.dcriar.domain.product.entity.Produto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * Interface MapStruct para mapear a entidade {@link Produto} para seu DTO de resposta.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = DimensoesMapper.class)
public interface ProdutoMapper {

    /**
     * Converte a entidade Produto para um DTO de resposta, incluindo sua composição.
     */
    @Mapping(target = "estoqueFisicoTotal", ignore = true)
    @Mapping(target = "estoqueDistribuidoTotal", ignore = true)
    @Mapping(target = "estoqueDisponivelParaAlocar", ignore = true)
    @Mapping(source = "dimensoesUnitarias", target = "dimensoes")
    ProdutoResponseDTO toResponseDTO(Produto produto);
}
