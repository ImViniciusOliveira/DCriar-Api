package com.dcriar.api.mapper.product;

import com.dcriar.api.dto.response.product.ComposicaoResponseDTO;
import com.dcriar.api.dto.response.product.ProdutoResponseDTO;
import com.dcriar.domain.product.entity.ComposicaoProduto;
import com.dcriar.domain.product.entity.Produto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * Interface MapStruct para mapear a entidade {@link Produto} para seu DTO de resposta.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProdutoMapper {

    /**
     * Converte a entidade Produto para um DTO de resposta, incluindo sua composição.
     */
    ProdutoResponseDTO toResponseDTO(Produto produto);

    /**
     * Mapeia um item da composição (entidade) para seu DTO de resposta.
     * <p>
     * CORREÇÃO: A origem (source) do mapeamento foi atualizada de "materiaPrima" para "tipoMateriaPrima",
     * alinhando o mapper com a refatoração da entidade ComposicaoProduto.
     *
     * @param composicaoProduto A entidade de composição a ser convertida.
     * @return O DTO de resposta da composição.
     */
    @Mapping(source = "tipoMateriaPrima.id", target = "materiaPrimaId")
    @Mapping(source = "tipoMateriaPrima.nome", target = "nomeMateriaPrima")
    ComposicaoResponseDTO toComposicaoResponseDTO(ComposicaoProduto composicaoProduto);
}

