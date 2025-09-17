package com.dcriar.api.controller.product;

import com.dcriar.api.dto.request.product.AjusteEstoqueRequestDTO;
import com.dcriar.api.dto.response.product.EstoqueResponseDTO;
import com.dcriar.domain.product.service.EstoqueProdutoService;
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
 * Controller responsável por expor os endpoints da API para o recurso de Estoque de Produtos Acabados.
 */
@RestController
@RequestMapping("/api/v1/estoques")
@RequiredArgsConstructor
@Tag(name = "Estoque - Produtos Acabados", description = "Endpoints para gerenciamento do estoque de produtos finalizados")
public class EstoqueProdutoController {

    private final EstoqueProdutoService estoqueProdutoService;

    @PostMapping("/ajustar")
    @Operation(summary = "Ajustar o estoque de um produto em um canal de venda")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estoque ajustado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos ou a operação resultaria em estoque negativo", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto ou Canal de Venda não encontrado", content = @Content)
    })
    public ResponseEntity<EstoqueResponseDTO> ajustarEstoque(@RequestBody @Valid AjusteEstoqueRequestDTO requestDTO) {
        EstoqueResponseDTO estoqueAtualizado = estoqueProdutoService.ajustarEstoque(requestDTO);
        return ResponseEntity.ok(estoqueAtualizado);
    }

    @GetMapping
    @Operation(summary = "Consultar o estoque de um produto em um canal específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estoque encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Registro de estoque, Produto ou Canal de Venda não encontrado", content = @Content)
    })
    public ResponseEntity<EstoqueResponseDTO> consultarEstoque(
            @RequestParam Long produtoId,
            @RequestParam Long canalVendaId) {
        EstoqueResponseDTO estoque = estoqueProdutoService.consultarEstoque(produtoId, canalVendaId);
        return ResponseEntity.ok(estoque);
    }

    @GetMapping("/por-produto/{produtoId}")
    @Operation(summary = "Listar todos os estoques de um produto em todos os canais")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de estoques retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content)
    })
    public ResponseEntity<List<EstoqueResponseDTO>> listarEstoquesPorProduto(@PathVariable Long produtoId) {
        List<EstoqueResponseDTO> estoques = estoqueProdutoService.listarEstoquesPorProduto(produtoId);
        return ResponseEntity.ok(estoques);
    }
}
