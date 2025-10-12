package com.dcriar.api.mapper.production;

import com.dcriar.api.dto.request.production.MargensRequestDTO;
import com.dcriar.api.dto.response.production.OrdemDeProducaoResponseDTO;
import com.dcriar.api.hateous.production.model.OrdemDeProducaoModel;
import com.dcriar.domain.production.entity.Margens;
import com.dcriar.domain.production.entity.OrdemDeProducao;
import com.dcriar.domain.stock.entity.LoteMateriaPrima;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Interface MapStruct para mapear a entidade {@link OrdemDeProducao} para seus DTOs e Models.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrdemDeProducaoMapper {

    /**
     * Converte a entidade OrdemDeProducao para um DTO de resposta.
     *
     * @param ordem A entidade a ser convertida.
     * @return O DTO de resposta correspondente.
     */
    @Mapping(target = "detalhesCorte", ignore = true)
    @Mapping(source = "produto.id", target = "produtoId")
    @Mapping(source = "produto.nome", target = "nomeProduto")
    @Mapping(source = "lotesConsumidos", target = "lotesConsumidosIds", qualifiedByName = "lotesToIds")
    OrdemDeProducaoResponseDTO toDto(OrdemDeProducao ordem);

    /**
     * Converte um DTO de resposta para o modelo de representação HATEOAS.
     * Como os nomes dos campos são idênticos, o MapStruct faz o mapeamento automaticamente.
     *
     * @param dto O DTO de resposta.
     * @return O Modelo HATEOAS correspondente.
     */
    OrdemDeProducaoModel toModel(OrdemDeProducaoResponseDTO dto);

    /**
     * Converte um DTO de requisição de margens para a entidade Margens.
     *
     * @param dto O DTO de requisição.
     * @return A entidade Margens.
     */
    Margens toMargensEntity(MargensRequestDTO dto);

    /**
     * Método qualificador para converter um Set de LoteMateriaPrima em uma Lista de seus IDs.
     *
     * @param lotes O conjunto de entidades de lote.
     * @return Uma lista contendo os IDs dos lotes.
     */
    @Named("lotesToIds")
    default List<Long> lotesToIds(Set<LoteMateriaPrima> lotes) {
        if (lotes == null || lotes.isEmpty()) {
            return Collections.emptyList();
        }
        return lotes.stream()
                .map(LoteMateriaPrima::getId)
                .collect(Collectors.toList());
    }
}
