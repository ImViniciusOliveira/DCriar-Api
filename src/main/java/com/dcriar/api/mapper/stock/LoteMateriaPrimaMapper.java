package com.dcriar.api.mapper.stock;

import com.dcriar.api.dto.response.stock.LoteMateriaPrimaResponseDTO;
import com.dcriar.domain.stock.entity.LoteMateriaPrima;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * Interface MapStruct para mapear a entidade {@link LoteMateriaPrima} para seus DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LoteMateriaPrimaMapper {

    /**
     * Converte a entidade LoteMateriaPrima para um DTO de resposta.
     * <p>
     * O campo 'saldoEstoque' é ignorado aqui, pois será calculado e definido
     * posteriormente na camada de serviço.
     *
     * @param lote A entidade de domínio a ser convertida.
     * @return O DTO de resposta correspondente.
     */
    @Mapping(source = "tipoMateriaPrima.nome", target = "nomeTipoMateriaPrima")
    // Adicionamos o mapeamento para o ID do lote de origem.
    // Se o loteDeOrigem for nulo, o MapStruct irá inteligentemente passar o valor nulo para o DTO.
    @Mapping(source = "loteDeOrigem.id", target = "loteDeOrigemId")
    @Mapping(target = "saldoEstoque", ignore = true)
    LoteMateriaPrimaResponseDTO toResponseDTO(LoteMateriaPrima lote);

}

