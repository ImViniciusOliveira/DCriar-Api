package com.dcriar.api.mapper.sales;

import com.dcriar.api.dto.response.sales.SaleItemResponseDTO;
import com.dcriar.api.dto.response.sales.SaleResponseDTO;
import com.dcriar.domain.sales.entity.Sale;
import com.dcriar.domain.sales.entity.SaleItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * Interface MapStruct para mapear as entidades do domínio de Vendas ({@link Sale} e {@link SaleItem})
 * para seus respectivos DTOs de resposta.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SaleMapper {

    /**
     * Converte a entidade {@link Sale} (cabeçalho da venda) para o seu DTO de resposta {@link SaleResponseDTO}.
     * <p>
     * O MapStruct irá inspecionar a lista {@code items} na entidade {@code Sale} e, para cada
     * {@link SaleItem}, invocará automaticamente o método {@link #toResponseDTO(SaleItem)} para
     * converter a lista de itens da venda.
     *
     * @param sale A entidade de venda a ser convertida.
     * @return O DTO de resposta da venda, incluindo a lista de itens convertida.
     */
    @Mapping(source = "canalVenda.nome", target = "nomeCanalVenda")
    SaleResponseDTO toResponseDTO(Sale sale);

    /**
     * Converte a entidade {@link SaleItem} (item da venda) para o seu DTO de resposta {@link SaleItemResponseDTO}.
     * <p>
     * Este método é usado tanto diretamente quanto indiretamente pelo mapeamento de {@link Sale} para {@link SaleResponseDTO}.
     *
     * @param saleItem A entidade de item de venda a ser convertida.
     * @return O DTO de resposta do item da venda.
     */
    @Mapping(source = "produto.id", target = "produtoId")
    @Mapping(source = "produto.sku", target = "produtoSku")
    @Mapping(source = "produto.nome", target = "nomeProduto")
    SaleItemResponseDTO toResponseDTO(SaleItem saleItem);
}
