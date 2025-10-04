package com.dcriar.api.mapper.sales;

import com.dcriar.api.dto.response.sales.SaleItemResponseDTO;
import com.dcriar.api.dto.response.sales.SaleResponseDTO;
import com.dcriar.domain.sales.entity.Sale;
import com.dcriar.domain.sales.entity.SaleItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * Interface MapStruct para mapear as entidades do domínio de Vendas para seus DTOs de resposta.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SaleMapper {

    /**
     * Converte a entidade Sale para o seu DTO de resposta.
     * O MapStruct irá automaticamente mapear a lista de itens usando o método auxiliar.
     */
    @Mapping(source = "canalVenda.nome", target = "nomeCanalVenda")
    SaleResponseDTO toResponseDTO(Sale sale);

    /**
     * Converte a entidade SaleItem para o seu DTO de resposta.
     */
    @Mapping(source = "produto.id", target = "produtoId")
    @Mapping(source = "produto.sku", target = "produtoSku")
    @Mapping(source = "produto.nome", target = "nomeProduto")
    SaleItemResponseDTO toResponseDTO(SaleItem saleItem);
}
