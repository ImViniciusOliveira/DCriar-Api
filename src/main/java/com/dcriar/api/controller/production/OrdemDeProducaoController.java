package com.dcriar.api.controller.production;

import com.dcriar.api.hateous.production.assembler.OrdemDeCorteModelAssembler;
import com.dcriar.api.dto.request.production.OrdemDeCorteRequestDTO;
import com.dcriar.api.dto.response.production.OrdemDeCorteResponseDTO;
import com.dcriar.api.hateous.production.model.OrdemDeCorteModel;
import com.dcriar.domain.production.service.OrdemDeProducaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller responsável por expor os endpoints relacionados a processos de produção.
 * Implementa o padrão HATEOAS para enriquecer as respostas com links navegáveis.
 */
@RestController
@RequestMapping("/api/v1/producao/ordens-de-corte")
@RequiredArgsConstructor
@Tag(name = "Produção - Ordens de Corte", description = "Endpoints para gerenciamento de ordens de corte")
public class OrdemDeProducaoController {

    private final OrdemDeProducaoService ordemDeProducaoService;
    private final OrdemDeCorteModelAssembler ordemDeCorteModelAssembler;

    /**
     * Processa e cria uma nova ordem de corte, consumindo material e gerando sobras automaticamente.
     * Retorna a ordem de corte processada com links HATEOAS.
     *
     * @param requestDTO O DTO com os detalhes da ordem de corte.
     * @return Uma resposta HTTP 201 (Created) com a localização do novo recurso e o {@link OrdemDeCorteModel} no corpo.
     */
    @PostMapping
    @Operation(summary = "Processar e criar uma ordem de corte",
            description = "Endpoint para automatizar o consumo de material de um rolo. Ele dá baixa no comprimento do lote principal e, se houver sobra, cria automaticamente um novo lote para o retalho.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ordem de corte criada com sucesso.",
                    headers = @Header(name = "Location", description = "URL do novo recurso")),
            @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos ou saldo/dimensões insuficientes.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Recurso relacionado (produto, lote) não encontrado.", content = @Content)
    })
    public ResponseEntity<OrdemDeCorteModel> processarOrdemDeCorte(@RequestBody @Valid OrdemDeCorteRequestDTO requestDTO) {
        OrdemDeCorteResponseDTO responseDTO = ordemDeProducaoService.processarOrdemDeCorte(requestDTO);
        return ordemDeCorteModelAssembler.toCreatedResponseEntity(responseDTO);
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
        OrdemDeCorteResponseDTO responseDTO = ordemDeProducaoService.buscarOrdemDeCortePorId(id);
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
        List<OrdemDeCorteResponseDTO> ordensDeCorteDTO = ordemDeProducaoService.listarTodasOrdensDeCorte();
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
