package com.dcriar.api.hateous.model;

import com.dcriar.api.dto.request.production.MargensRequestDTO;
import com.dcriar.api.dto.response.production.CorteRealizadoDTO;
import com.dcriar.api.dto.response.production.OrdemDeCorteResponseDTO;
import com.dcriar.api.dto.response.production.TamanhoFinalResponseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.util.List;

/**
 * Representa o modelo de recurso HATEOAS para uma Ordem de Corte.
 * Este modelo estende {@link RepresentationModel} para incluir links HATEOAS
 * e é usado para representar informações detalhadas sobre uma ordem de corte específica.
 */
@Getter
@Setter
@Builder
@Relation(collectionRelation = "ordensDeCorte")
public class OrdemDeCorteModel extends RepresentationModel<OrdemDeCorteModel> {

    /**
     * O ID único da ordem de corte.
     */
    private Long id;

    /**
     * O ID do produto a ser produzido.
     */
    private Long produtoId;

    /**
     * O ID do lote principal de matéria-prima utilizado.
     */
    private Long lotePrincipalId;

    /**
     * O ID do canal de venda para o qual o estoque será destinado.
     */
    private Long canalVendaDestinoId;

    /**
     * A quantidade de unidades do produto que foram efetivamente produzidas.
     */
    private Integer quantidadeProduzida;

    /**
     * A justificativa ou observação para a ordem de corte.
     */
    private String motivo;

    /**
     * O modo de cálculo utilizado para a otimização do corte (ex: 'AUTOMÁTICO', 'MANUAL').
     */
    private String modoCalculo;

    /**
     * As margens de segurança e de corte configuradas para esta ordem.
     */
    private MargensRequestDTO margens;

    /**
     * As dimensões finais do produto após o corte.
     */
    private TamanhoFinalResponseDTO tamanhoFinal;

    /**
     * A lista de cortes realizados, detalhando cada faixa produzida ou retalho gerado.
     */
    private List<CorteRealizadoDTO> cortesRealizados;

    /**
     * Construtor privado para uso do Lombok Builder.
     *
     * @param id O ID único da ordem de corte.
     * @param produtoId O ID do produto a ser produzido.
     * @param lotePrincipalId O ID do lote principal de matéria-prima utilizado.
     * @param canalVendaDestinoId O ID do canal de venda para o qual o estoque será destinado.
     * @param quantidadeProduzida A quantidade de unidades do produto que foram efetivamente produzidas.
     * @param motivo A justificativa ou observação para a ordem de corte.
     * @param modoCalculo O modo de cálculo utilizado para a otimização do corte.
     * @param margens As margens de segurança e de corte configuradas para esta ordem.
     * @param tamanhoFinal As dimensões finais do produto após o corte.
     * @param cortesRealizados A lista de cortes realizados.
     */
    private OrdemDeCorteModel(Long id, Long produtoId, Long lotePrincipalId, Long canalVendaDestinoId,
                              Integer quantidadeProduzida, String motivo, String modoCalculo,
                              MargensRequestDTO margens, TamanhoFinalResponseDTO tamanhoFinal,
                              List<CorteRealizadoDTO> cortesRealizados) {
        this.id = id;
        this.produtoId = produtoId;
        this.lotePrincipalId = lotePrincipalId;
        this.canalVendaDestinoId = canalVendaDestinoId;
        this.quantidadeProduzida = quantidadeProduzida;
        this.motivo = motivo;
        this.modoCalculo = modoCalculo;
        this.margens = margens;
        this.tamanhoFinal = tamanhoFinal;
        this.cortesRealizados = cortesRealizados;
    }

    /**
     * Cria uma instância de {@link OrdemDeCorteModel} a partir de um {@link OrdemDeCorteResponseDTO}.
     * Utiliza o padrão Builder para construir o objeto de forma mais legível e flexível.
     *
     * @param dto O DTO de resposta da ordem de corte contendo os dados.
     * @return Uma nova instância de {@link OrdemDeCorteModel} preenchida com os dados do DTO.
     */
    public static OrdemDeCorteModel fromDto(OrdemDeCorteResponseDTO dto) {
        return OrdemDeCorteModel.builder()
                .id(dto.getId())
                .produtoId(dto.getProdutoId())
                .lotePrincipalId(dto.getLotePrincipalId())
                .canalVendaDestinoId(dto.getCanalVendaDestinoId())
                .quantidadeProduzida(dto.getQuantidadeProduzida())
                .motivo(dto.getMotivo())
                .modoCalculo(dto.getModoCalculo())
                .margens(dto.getMargens())
                .tamanhoFinal(dto.getTamanhoFinal())
                .cortesRealizados(dto.getCortesRealizados())
                .build();
    }
}
