package com.dcriar.api.mapper.stock;

import com.dcriar.api.dto.response.stock.MovimentacaoResponseDTO;
import com.dcriar.domain.stock.entity.MovimentacaoEstoqueLote;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * Interface MapStruct para mapear a entidade {@link MovimentacaoEstoqueLote}
 * para seu DTO de resposta.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MovimentacaoMapper {

    /**
     * Converte a entidade MovimentacaoEstoqueLote para um DTO de resposta.
     * <p>
     * O MapStruct irá mapear automaticamente todos os campos com nomes correspondentes,
     * incluindo o novo campo 'custoPorUnidadeBase'.
     *
     * @param movimentacao A entidade de origem.
     * @return O DTO {@link MovimentacaoResponseDTO} correspondente.
     */
    MovimentacaoResponseDTO toResponseDTO(MovimentacaoEstoqueLote movimentacao);
}