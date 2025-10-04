package com.dcriar.api.controller.product;

import com.dcriar.api.hateous.product.assembler.EstoqueProdutoModelAssembler;
import com.dcriar.api.hateous.product.assembler.MovimentacaoProdutoModelAssembler;
import com.dcriar.api.dto.request.product.AjusteEstoqueProdutoRequestDTO;
import com.dcriar.api.dto.request.product.AjusteEstoqueRequestDTO;
import com.dcriar.api.dto.response.product.EstoqueResponseDTO;
import com.dcriar.api.dto.response.product.MovimentacaoProdutoResponseDTO;
import com.dcriar.api.hateous.product.model.EstoqueProdutoModel;
import com.dcriar.api.hateous.product.model.MovimentacaoProdutoModel;
import com.dcriar.domain.product.service.EstoqueProdutoService;
import io.swagger.v3.oas.annotations.Operation;
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
 * Controller responsável por expor os endpoints da API para o recurso de Estoque de Produtos Acabados.
 * Implementa o padrão HATEOAS para enriquecer as respostas com links navegáveis.
 */
@RestController
@RequestMapping("/api/v1/estoques")
@RequiredArgsConstructor
@Tag(name = "Estoque - Produtos Acabados", description = "Endpoints para gerenciamento do estoque de produtos finalizados")
public class EstoqueProdutoController {

    private final EstoqueProdutoService estoqueProdutoService;
    private final EstoqueProdutoModelAssembler estoqueProdutoModelAssembler;
    private final MovimentacaoProdutoModelAssembler movimentacaoProdutoModelAssembler;

    @PostMapping("/ajustar-canal")
    @Operation(summary = "Ajustar o estoque de um produto em um canal de venda (distribuição)")
    public ResponseEntity<EstoqueProdutoModel> ajustarEstoqueCanal(@RequestBody @Valid AjusteEstoqueRequestDTO requestDTO) {
        EstoqueResponseDTO estoqueAtualizadoDTO = estoqueProdutoService.ajustarEstoque(requestDTO);
        return estoqueProdutoModelAssembler.toOkResponseEntity(estoqueAtualizadoDTO);
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
    public ResponseEntity<EstoqueProdutoModel> consultarEstoque(
            @RequestParam Long produtoId,
            @RequestParam Long canalVendaId) {
        EstoqueResponseDTO estoqueDTO = estoqueProdutoService.consultarEstoque(produtoId, canalVendaId);
        return estoqueProdutoModelAssembler.toOkResponseEntity(estoqueDTO);
    }

    @GetMapping("/por-produto/{produtoId}")
    @Operation(summary = "Listar todos os estoques de um produto em todos os canais")
    public ResponseEntity<CollectionModel<EstoqueProdutoModel>> listarEstoquesPorProduto(@PathVariable Long produtoId) {
        List<EstoqueResponseDTO> estoquesDTO = estoqueProdutoService.listarEstoquesPorProduto(produtoId);
        return estoqueProdutoModelAssembler.toOkResponseEntity(estoquesDTO, produtoId);
    }

    @GetMapping("/fisico/por-produto/{produtoId}")
    @Operation(summary = "Consultar o histórico do Estoque Físico Total de um produto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Histórico retornado com sucesso."),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado.", content = @Content)
    })
    public ResponseEntity<CollectionModel<MovimentacaoProdutoModel>> listarMovimentacoesPorProduto(@PathVariable Long produtoId) {
        List<MovimentacaoProdutoResponseDTO> historicoDTO = estoqueProdutoService.listarMovimentacoesPorProduto(produtoId);
        return movimentacaoProdutoModelAssembler.toOkResponseEntity(historicoDTO, produtoId);
    }
}
