package com.dcriar.api.controller.stock;

import com.dcriar.api.dto.request.stock.LoteMateriaPrimaRequestDTO;
import com.dcriar.api.dto.request.stock.MovimentacaoRequestDTO;
import com.dcriar.api.dto.response.stock.LoteMateriaPrimaResponseDTO;
import com.dcriar.api.dto.response.stock.MovimentacaoResponseDTO;
import com.dcriar.domain.stock.service.LoteMateriaPrimaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Controller responsável por expor os endpoints da API para o recurso de Lote de Matéria-Prima.
 */
@RestController
@RequestMapping("/api/v1/lotes-materia-prima")
@RequiredArgsConstructor
@Tag(name = "Estoque - Lotes", description = "Endpoints para gerenciamento de lotes físicos de matéria-prima")
public class LoteMateriaPrimaController {

    private final LoteMateriaPrimaService loteMateriaPrimaService;

    @PostMapping
    @Operation(summary = "Dar entrada de um novo lote no estoque")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Lote criado com sucesso",
                    headers = @Header(name = "Location", description = "URL do novo recurso")),
            @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos", content = @Content)
    })
    public ResponseEntity<LoteMateriaPrimaResponseDTO> create(@RequestBody @Valid LoteMateriaPrimaRequestDTO requestDTO) {
        LoteMateriaPrimaResponseDTO loteCriado = loteMateriaPrimaService.create(requestDTO);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(loteCriado.getId()).toUri();
        return ResponseEntity.created(location).body(loteCriado);
    }

    @GetMapping
    @Operation(summary = "Listar todos os lotes de matéria-prima")
    @ApiResponse(responseCode = "200", description = "Lista de lotes retornada com sucesso")
    public ResponseEntity<List<LoteMateriaPrimaResponseDTO>> findAll() {
        return ResponseEntity.ok(loteMateriaPrimaService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar um lote por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lote encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Lote não encontrado", content = @Content)
    })
    public ResponseEntity<LoteMateriaPrimaResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(loteMateriaPrimaService.findById(id));
    }

    @PostMapping("/{loteId}/movimentacoes")
    @Operation(summary = "Registar uma nova movimentação em um lote")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Movimentação registada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou saldo insuficiente", content = @Content),
            @ApiResponse(responseCode = "404", description = "Lote não encontrado", content = @Content)
    })
    public ResponseEntity<MovimentacaoResponseDTO> registrarMovimentacao(
            @PathVariable Long loteId,
            @RequestBody @Valid MovimentacaoRequestDTO requestDTO) {
        MovimentacaoResponseDTO movimentacao = loteMateriaPrimaService.registrarMovimentacao(loteId, requestDTO);
        return ResponseEntity.status(201).body(movimentacao);
    }

    @GetMapping("/{loteId}/movimentacoes")
    @Operation(summary = "Consultar o histórico de movimentações de um lote")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Histórico retornado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Lote não encontrado", content = @Content)
    })
    public ResponseEntity<List<MovimentacaoResponseDTO>> listarMovimentacoes(@PathVariable Long loteId) {
        return ResponseEntity.ok(loteMateriaPrimaService.listarMovimentacoesPorLote(loteId));
    }
}

