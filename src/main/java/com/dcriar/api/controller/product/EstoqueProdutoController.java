package com.dcriar.api.controller.product;

import com.dcriar.api.hateous.assembler.EstoqueProdutoModelAssembler;
import com.dcriar.api.hateous.assembler.MovimentacaoProdutoModelAssembler;
import com.dcriar.api.dto.request.product.AjusteEstoqueProdutoRequestDTO;
import com.dcriar.api.dto.request.product.AjusteEstoqueRequestDTO;
import com.dcriar.api.dto.response.product.EstoqueResponseDTO;
import com.dcriar.api.dto.response.product.MovimentacaoProdutoResponseDTO;
import com.dcriar.api.hateous.model.EstoqueProdutoModel;
import com.dcriar.api.hateous.model.MovimentacaoProdutoModel;
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
    private final MovimentacaoProdutoModelAssembler movimentacaoProdutoModelAssembler; // Injetando o novo assembler

    /**
     * Ajusta a quantidade em estoque de um produto em um canal de venda específico.
     * Após o ajuste, retorna o estado atualizado do estoque com links HATEOAS.
     *
     * @param requestDTO DTO contendo as informações para o ajuste de estoque no canal.
     * @return ResponseEntity contendo o {@link EstoqueProdutoModel} atualizado com links.
     */
    @PostMapping("/ajustar-canal")
    @Operation(summary = "Ajustar o estoque de um produto em um canal de venda (distribuição)")
    public ResponseEntity<EstoqueProdutoModel> ajustarEstoqueCanal(@RequestBody @Valid AjusteEstoqueRequestDTO requestDTO) {
        EstoqueResponseDTO estoqueAtualizadoDTO = estoqueProdutoService.ajustarEstoque(requestDTO);
        EstoqueProdutoModel estoqueModel = estoqueProdutoModelAssembler.toModel(estoqueAtualizadoDTO);
        return ResponseEntity.ok(estoqueModel);
    }

    /**
     * Ajusta o estoque físico total (mestre) de um produto.
     * Esta operação não retorna um corpo de resposta, apenas um status de sucesso.
     *
     * @param requestDTO DTO contendo as informações para o ajuste do estoque físico.
     * @return ResponseEntity com status 204 No Content em caso de sucesso.
     */
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

    /**
     * Consulta o estoque de um produto em um canal de venda específico.
     * Retorna o estoque encontrado com links HATEOAS.
     *
     * @param produtoId O ID do produto.
     * @param canalVendaId O ID do canal de venda.
     * @return ResponseEntity contendo o {@link EstoqueProdutoModel} com links.
     */
    @GetMapping
    @Operation(summary = "Consultar o estoque de um produto em um canal específico")
    public ResponseEntity<EstoqueProdutoModel> consultarEstoque(
            @RequestParam Long produtoId,
            @RequestParam Long canalVendaId) {
        EstoqueResponseDTO estoqueDTO = estoqueProdutoService.consultarEstoque(produtoId, canalVendaId);
        EstoqueProdutoModel estoqueModel = estoqueProdutoModelAssembler.toModel(estoqueDTO);
        return ResponseEntity.ok(estoqueModel);
    }

    /**
     * Lista todos os registros de estoque de um produto em todos os canais de venda.
     * Retorna uma coleção de {@link EstoqueProdutoModel} com links HATEOAS.
     *
     * @param produtoId O ID do produto para o qual listar os estoques.
     * @return ResponseEntity contendo um {@link CollectionModel} de {@link EstoqueProdutoModel} com links.
     */
    @GetMapping("/por-produto/{produtoId}")
    @Operation(summary = "Listar todos os estoques de um produto em todos os canais")
    public ResponseEntity<CollectionModel<EstoqueProdutoModel>> listarEstoquesPorProduto(@PathVariable Long produtoId) {
        List<EstoqueResponseDTO> estoquesDTO = estoqueProdutoService.listarEstoquesPorProduto(produtoId);
        CollectionModel<EstoqueProdutoModel> collectionModel = estoqueProdutoModelAssembler.toCollectionModel(estoquesDTO, produtoId);
        return ResponseEntity.ok(collectionModel);
    }

    /**
     * Consulta o histórico completo de movimentações do Estoque Físico Total (o "Livro-Razão") para um produto.
     * Retorna uma coleção de {@link MovimentacaoProdutoModel} com links HATEOAS.
     *
     * @param produtoId O ID do produto para o qual consultar o histórico de movimentações.
     * @return ResponseEntity contendo uma {@link CollectionModel} de {@link MovimentacaoProdutoModel}.
     */
    @GetMapping("/fisico/por-produto/{produtoId}")
    @Operation(summary = "Consultar o histórico do Estoque Físico Total de um produto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Histórico retornado com sucesso."),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado.", content = @Content)
    })
    public ResponseEntity<CollectionModel<MovimentacaoProdutoModel>> listarMovimentacoesPorProduto(@PathVariable Long produtoId) {
        List<MovimentacaoProdutoResponseDTO> historicoDTO = estoqueProdutoService.listarMovimentacoesPorProduto(produtoId);
        CollectionModel<MovimentacaoProdutoModel> collectionModel = movimentacaoProdutoModelAssembler.toCollectionModel(historicoDTO, produtoId);
        return ResponseEntity.ok(collectionModel);
    }
}
