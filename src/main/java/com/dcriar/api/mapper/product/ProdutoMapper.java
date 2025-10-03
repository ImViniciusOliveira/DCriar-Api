package com.dcriar.api.mapper.product;

import com.dcriar.api.dto.response.product.ProdutoResponseDTO;
import com.dcriar.api.hateous.model.ProdutoModel;
import com.dcriar.domain.product.entity.Produto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * Interface MapStruct para mapear a entidade {@link Produto} para seus DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = DimensoesMapper.class)
public interface ProdutoMapper {

    /**
     * Converte a entidade Produto para um DTO de resposta.
     * Os campos de estoque são ignorados pois são enriquecidos posteriormente pelo serviço.
     */
    @Mapping(target = "estoqueFisicoTotal", ignore = true)
    @Mapping(target = "estoqueDistribuidoTotal", ignore = true)
    @Mapping(target = "estoqueDisponivelParaAlocar", ignore = true)
    @Mapping(source = "dimensoesUnitarias", target = "dimensoes")
    ProdutoResponseDTO toResponseDTO(Produto produto);

    /**
     * Converte um DTO de resposta para o modelo de representação HATEOAS.
     */
    ProdutoModel toModel(ProdutoResponseDTO responseDTO);
}
