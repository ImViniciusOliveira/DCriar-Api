package com.dcriar.api.controller.sales;

import com.dcriar.api.dto.request.sales.SaleRequestDTO;
import com.dcriar.api.dto.response.sales.SaleResponseDTO;
import com.dcriar.domain.sales.service.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller responsável por expor os endpoints da API para o recurso de Vendas (Sales).
 */
@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
@Tag(name = "Vendas", description = "Endpoints para o registo e consulta de vendas")
public class SaleController {

    private final SaleService saleService;

    /**
     * Regista uma nova venda e orquestra a baixa automática de estoque.
     */
    @PostMapping
    @Operation(summary = "Registar uma nova venda")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Venda registada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos ou estoque insuficiente.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto ou Canal de Venda não encontrado.", content = @Content)
    })
    public ResponseEntity<SaleResponseDTO> registerSale(@RequestBody @Valid SaleRequestDTO requestDTO) {
        SaleResponseDTO registeredSale = saleService.registerSale(requestDTO);
        return ResponseEntity.status(201).body(registeredSale);
    }

    /**
     * Lista todas as vendas registadas no sistema.
     */
    @GetMapping
    @Operation(summary = "Listar todas as vendas")
    @ApiResponse(responseCode = "200", description = "Lista de vendas retornada com sucesso.")
    public ResponseEntity<List<SaleResponseDTO>> findAll() {
        return ResponseEntity.ok(saleService.findAll());
    }

    /**
     * Busca os detalhes de uma venda específica pelo seu ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar uma venda por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Venda encontrada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Venda não encontrada.", content = @Content)
    })
    public ResponseEntity<SaleResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.findById(id));
    }
}

