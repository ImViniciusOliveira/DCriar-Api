package com.dcriar.api.controller.product;

import com.dcriar.api.dto.request.product.AjusteEstoqueProdutoRequestDTO;
import com.dcriar.api.dto.request.product.AjusteEstoqueRequestDTO;
import com.dcriar.api.dto.response.product.EstoqueResponseDTO;
import com.dcriar.api.dto.response.product.MovimentacaoProdutoResponseDTO;
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

    @PostMapping("/ajustar-canal")
    @Operation(summary = "Ajustar o estoque de um produto em um canal de venda (distribuição)")
    public ResponseEntity<EstoqueResponseDTO> ajustarEstoqueCanal(@RequestBody @Valid AjusteEstoqueRequestDTO requestDTO) {
        EstoqueResponseDTO estoqueAtualizado = estoqueProdutoService.ajustarEstoque(requestDTO);
        return ResponseEntity.ok(estoqueAtualizado);
    }

    @PostMapping("/ajuste-fisico")
    @Operation(summary = "Ajustar o Estoque Físico Total de um produto (o \"Estoque Mestre\")")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Estoque físico ajustado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado.", content = @Content)
    })
    public ResponseEntity<Void> ajustarEstoqueFisico(@RequestBody @Valid AjusteEstoqueProdutoRequestDTO requestDTO) {
        estoqueProdutoService.ajustarEstoqueFisico(requestDTO);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Consultar o estoque de um produto em um canal específico")
    public ResponseEntity<EstoqueResponseDTO> consultarEstoque(
            @RequestParam Long produtoId,
            @RequestParam Long canalVendaId) {
        EstoqueResponseDTO estoque = estoqueProdutoService.consultarEstoque(produtoId, canalVendaId);
        return ResponseEntity.ok(estoque);
    }

    @GetMapping("/por-produto/{produtoId}")
    @Operation(summary = "Listar todos os estoques de um produto em todos os canais")
    public ResponseEntity<List<EstoqueResponseDTO>> listarEstoquesPorProduto(@PathVariable Long produtoId) {
        List<EstoqueResponseDTO> estoques = estoqueProdutoService.listarEstoquesPorProduto(produtoId);
        return ResponseEntity.ok(estoques);
    }

    /**
     *Consulta o histórico completo de movimentações do Estoque Físico Total (o "Livro-Razão").
     */
    @GetMapping("/fisico/por-produto/{produtoId}")
    @Operation(summary = "Consultar o histórico do Estoque Físico Total de um produto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Histórico retornado com sucesso."),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado.", content = @Content)
    })
    public ResponseEntity<List<MovimentacaoProdutoResponseDTO>> listarMovimentacoesPorProduto(@PathVariable Long produtoId) {
        List<MovimentacaoProdutoResponseDTO> historico = estoqueProdutoService.listarMovimentacoesPorProduto(produtoId);
        return ResponseEntity.ok(historico);
    }
}

