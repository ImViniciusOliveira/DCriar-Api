package com.dcriar.api.controller.production;

import com.dcriar.api.dto.request.production.OrdemDeCorteRequestDTO;
import com.dcriar.domain.production.service.OrdemDeProducaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller responsável por expor os endpoints relacionados a processos de produção.
 */
@RestController
@RequestMapping("/api/v1/producao")
@RequiredArgsConstructor
@Tag(name = "Produção", description = "Endpoints para automatizar processos de produção")
public class OrdemDeProducaoController {

    private final OrdemDeProducaoService ordemDeProducaoService;

    /**
     * Processa uma ordem de corte, consumindo material e gerando sobras automaticamente.
     *
     * @param requestDTO O DTO com os detalhes da ordem de corte.
     * @return Uma resposta HTTP 204 (No Content) indicando que a operação foi bem-sucedida.
     */
    @PostMapping("/ordens-de-corte")
    @Operation(summary = "Processar uma ordem de corte",
            description = "Endpoint para automatizar o consumo de material de um rolo. Ele dá baixa no comprimento do lote principal e, se houver sobra, cria automaticamente um novo lote para o retalho.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Ordem de corte processada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos ou saldo/dimensões insuficientes.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Lote de matéria-prima principal não encontrado.", content = @Content)
    })
    public ResponseEntity<Void> processarOrdemDeCorte(@RequestBody @Valid OrdemDeCorteRequestDTO requestDTO) {
        ordemDeProducaoService.processarOrdemDeCorte(requestDTO);
        return ResponseEntity.noContent().build();
    }
}

