package com.dcriar.api.dto.response.production;

import com.dcriar.api.dto.request.production.MargensRequestDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * Data Transfer Object (DTO) que representa a resposta de uma Ordem de Corte.
 * <p>
 * Este DTO consolida todas as informações de uma ordem de corte, incluindo os
 * materiais utilizados, as especificações de produção e os resultados do corte.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdemDeCorteResponseDTO {

    @Schema(description = "O ID único da ordem de corte.", example = "1")
    private Long id;

    @Schema(description = "O ID do produto a ser produzido.", example = "101")
    private Long produtoId;

    @Schema(description = "O ID do lote principal de matéria-prima utilizado.", example = "201")
    private Long lotePrincipalId;

    @Schema(description = "O ID do canal de venda para o qual o estoque será destinado.", example = "301")
    private Long canalVendaDestinoId;

    @Schema(description = "A quantidade de unidades do produto que foram efetivamente produzidas.", example = "100")
    private Integer quantidadeProduzida;

    @Schema(description = "A justificativa ou observação para a ordem de corte.", example = "Produção para atender pedido #123")
    private String motivo;

    @Schema(description = "O modo de cálculo utilizado para a otimização do corte (ex: 'AUTOMÁTICO', 'MANUAL').", example = "AUTOMÁTICO")
    private String modoCalculo;

    @Schema(description = "As margens de segurança e de corte configuradas para esta ordem.")
    private MargensRequestDTO margens;

    @Schema(description = "As dimensões finais do produto após o corte.")
    private TamanhoFinalResponseDTO tamanhoFinal;

    @Schema(description = "A lista de cortes realizados, detalhando cada faixa produzida ou retalho gerado.")
    private List<CorteRealizadoDTO> cortesRealizados;
}
