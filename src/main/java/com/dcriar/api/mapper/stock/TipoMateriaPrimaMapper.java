package com.dcriar.api.mapper.stock;

import com.dcriar.api.dto.request.stock.TipoMateriaPrimaRequestDTO;
import com.dcriar.api.dto.response.stock.TipoMateriaPrimaResponseDTO;
import com.dcriar.domain.stock.entity.TipoMateriaPrima;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * Interface MapStruct para mapear entre a entidade {@link TipoMateriaPrima} e seus DTOs.
 * <p>
 * Abstrai a lógica de conversão, mantendo o código limpo e com baixo acoplamento.
 * A anotação {@code componentModel = "spring"} permite que o Spring gerencie
 * a implementação gerada como um Bean, facilitando a injeção de dependência.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TipoMateriaPrimaMapper {

    /**
     * Converte um DTO de requisição para a entidade TipoMateriaPrima.
     * <p>
     * Ignoramos o campo 'id' pois ele será gerado pelo banco de dados e não
     * deve ser fornecido pelo cliente na criação.
     *
     * @param requestDTO O DTO de entrada.
     * @return A entidade {@link TipoMateriaPrima} correspondente, pronta para ser persistida.
     */
    @Mapping(target = "id", ignore = true)
    TipoMateriaPrima toEntity(TipoMateriaPrimaRequestDTO requestDTO);

    /**
     * Converte a entidade TipoMateriaPrima para um DTO de resposta.
     *
     * @param tipoMateriaPrima A entidade de origem.
     * @return O DTO {@link TipoMateriaPrimaResponseDTO} correspondente.
     */
    TipoMateriaPrimaResponseDTO toResponseDTO(TipoMateriaPrima tipoMateriaPrima);
}
