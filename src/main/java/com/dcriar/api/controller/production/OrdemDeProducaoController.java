package com.dcriar.api.controller.production;

import com.dcriar.api.hateous.assembler.OrdemDeCorteModelAssembler;
import com.dcriar.api.dto.request.production.OrdemDeCorteRequestDTO;
import com.dcriar.api.dto.response.production.OrdemDeCorteResponseDTO;
import com.dcriar.api.hateous.model.OrdemDeCorteModel;
import com.dcriar.domain.production.service.OrdemDeProducaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller responsável por expor os endpoints relacionados a processos de produção.
 * Implementa o padrão HATEOAS para enriquecer as respostas com links navegáveis.
 */
@RestController
@RequestMapping("/api/v1/producao/ordens-de-corte") // Ajustado o RequestMapping para ser mais específico
@RequiredArgsConstructor
@Tag(name = "Produção - Ordens de Corte", description = "Endpoints para gerenciamento de ordens de corte")
public class OrdemDeProducaoController {

    private final OrdemDeProducaoService ordemDeProducaoService;
    private final OrdemDeCorteModelAssembler ordemDeCorteModelAssembler;

    /**
     * Processa uma ordem de corte, consumindo material e gerando sobras automaticamente.
     * Retorna a ordem de corte processada com links HATEOAS.
     *
     * @param requestDTO O DTO com os detalhes da ordem de corte.
     * @return Uma resposta HTTP 200 (OK) contendo o {@link OrdemDeCorteModel} com links.
     */
    @PostMapping
    @Operation(summary = "Processar uma ordem de corte",
            description = "Endpoint para automatizar o consumo de material de um rolo. Ele dá baixa no comprimento do lote principal e, se houver sobra, cria automaticamente um novo lote para o retalho.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ordem de corte processada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos ou saldo/dimensões insuficientes.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Lote de matéria-prima principal não encontrado.", content = @Content)
    })
    public ResponseEntity<OrdemDeCorteModel> processarOrdemDeCorte(@RequestBody @Valid OrdemDeCorteRequestDTO requestDTO) {
        OrdemDeCorteResponseDTO responseDTO = ordemDeProducaoService.processarOrdemDeCorte(requestDTO);
        OrdemDeCorteModel ordemDeCorteModel = ordemDeCorteModelAssembler.toModel(responseDTO);
        return ResponseEntity.ok(ordemDeCorteModel);
    }

    /**
     * Busca uma ordem de corte específica pelo seu ID.
     * Retorna a ordem de corte encontrada com links HATEOAS.
     *
     * @param id O ID da ordem de corte a ser buscada.
     * @return ResponseEntity contendo o {@link OrdemDeCorteModel} com links.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar ordem de corte por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ordem de corte encontrada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Ordem de corte não encontrada.", content = @Content)
    })
    public ResponseEntity<OrdemDeCorteModel> buscarOrdemDeCortePorId(@PathVariable Long id) {
        OrdemDeCorteResponseDTO responseDTO = ordemDeProducaoService.buscarOrdemDeCortePorId(id); // Assumindo que este método existe no service
        OrdemDeCorteModel ordemDeCorteModel = ordemDeCorteModelAssembler.toModel(responseDTO);
        return ResponseEntity.ok(ordemDeCorteModel);
    }

    /**
     * Lista todas as ordens de corte registradas.
     * Retorna uma coleção de {@link OrdemDeCorteModel} com links HATEOAS.
     *
     * @return ResponseEntity contendo uma {@link CollectionModel} de {@link OrdemDeCorteModel}.
     */
    @GetMapping
    @Operation(summary = "Listar todas as ordens de corte")
    @ApiResponse(responseCode = "200", description = "Lista de ordens de corte retornada com sucesso.")
    public ResponseEntity<CollectionModel<OrdemDeCorteModel>> listarOrdensDeCorte() {
        List<OrdemDeCorteResponseDTO> ordensDeCorteDTO = ordemDeProducaoService.listarTodasOrdensDeCorte(); // Assumindo que este método existe no service
        CollectionModel<OrdemDeCorteModel> collectionModel = ordemDeCorteModelAssembler.toCollectionModel(ordensDeCorteDTO);
        return ResponseEntity.ok(collectionModel);
    }

    /**
     * Exclui uma ordem de corte pelo id.
     *
     * @param id O id da ordem de corte a ser excluída.
     * @return HTTP 204 se excluído com sucesso.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir uma ordem de corte",
            description = "Remove uma ordem de corte do sistema pelo id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Ordem de corte excluída com sucesso."),
            @ApiResponse(responseCode = "404", description = "Ordem de corte não encontrada.", content = @Content)
    })
    public ResponseEntity<Void> excluirOrdemDeCorte(@PathVariable Long id) {
        ordemDeProducaoService.excluirOrdemDeCorte(id);
        return ResponseEntity.noContent().build();
    }
}
