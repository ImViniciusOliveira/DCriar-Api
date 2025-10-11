package com.dcriar.api.controller.product;

import com.dcriar.api.dto.request.product.ProdutoRequestDTO;
import com.dcriar.api.dto.response.product.ProdutoResponseDTO;
import com.dcriar.api.hateous.product.assembler.ProdutoModelAssembler;
import com.dcriar.api.hateous.product.model.ProdutoModel;
import com.dcriar.domain.product.service.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller responsável por expor os endpoints da API para o recurso de Produto.
 */
@RestController
@RequestMapping("/api/v1/produtos")
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "Endpoints para gerenciamento de produtos")
public class ProdutoController {

    private final ProdutoService produtoService;
    private final ProdutoModelAssembler produtoModelAssembler;

    @GetMapping
    @Operation(summary = "Listar todos os produtos de forma paginada")
    @ApiResponse(responseCode = "200", description = "Lista de produtos retornada com sucesso")
    public PagedModel<ProdutoModel> findAll(@ParameterObject @PageableDefault(sort = "nome", direction = Sort.Direction.ASC) Pageable pageable, PagedResourcesAssembler<ProdutoResponseDTO> pagedResourcesAssembler) {
        Page<ProdutoResponseDTO> produtosPage = produtoService.findAll(pageable);
        return pagedResourcesAssembler.toModel(produtosPage, produtoModelAssembler);
    }

    /**
     * Método de sobrecarga para a construção de links HATEOAS.
     * Não é um endpoint e não deve ser chamado diretamente.
     */
    public PagedModel<ProdutoModel> findAll() {
        return null;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content)
    })
    public ProdutoModel findById(@PathVariable Long id) {
        ProdutoResponseDTO produto = produtoService.findById(id);
        return produtoModelAssembler.toModel(produto);
    }

    @PostMapping
    @Operation(summary = "Criar um novo produto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Produto criado com sucesso",
                    headers = @Header(name = "Location", description = "URL do novo recurso")),
            @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos", content = @Content)
    })
    public ResponseEntity<ProdutoModel> create(@RequestBody @Valid ProdutoRequestDTO requestDTO) {
        ProdutoResponseDTO produtoCriado = produtoService.create(requestDTO);
        return produtoModelAssembler.toCreatedResponseEntity(produtoCriado);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar parcialmente um produto existente (PATCH)",
            description = "Este método permite a atualização de um ou mais campos de um produto. Envie apenas os campos que deseja alterar.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content)
    })
    public ProdutoModel update(@PathVariable Long id,
                             @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                     description = "Corpo da requisição para atualização parcial. Envie apenas os campos que deseja alterar.",
                                     content = @Content(mediaType = "application/json",
                                             schema = @Schema(implementation = ProdutoRequestDTO.class),
                                             examples = {
                                                     @ExampleObject(
                                                             name = "Exemplo Completo",
                                                             summary = "Referência de todos os campos",
                                                             description = "Este exemplo mostra todos os campos que podem ser atualizados. Em uma requisição PATCH real, você normalmente enviaria apenas os campos que mudaram.",
                                                             value = "{\"nome\": \"Etiqueta Adesiva 10x15cm Couchê\", \"sku\": \"ETQ-COU-10X15\", \"descricao\": \"Etiqueta de papel couchê com acabamento brilhante.\", \"cor\": \"Branco\", \"unidadesPorProduto\": 500, \"fotoPrincipalUrl\": \"\", \"ativo\": true, \"tipoMateriaPrimaId\": 2, \"dimensoesUnitarias\": {\"larguraCm\": 10, \"comprimentoCm\": 15}}"
                                                     ),
                                                     @ExampleObject(
                                                             name = "Exemplo Parcial (Apenas Foto)",
                                                             summary = "Atualização de um único campo",
                                                             description = "Este é um exemplo comum, onde apenas a URL da foto principal é atualizada após um upload.",
                                                             value = "{\"fotoPrincipalUrl\": \"\"}"
                                                     )
                                             }
                                     )
                             )
                             @RequestBody Map<String, Object> fields) {
        ProdutoResponseDTO produtoAtualizado = produtoService.patch(id, fields);
        return produtoModelAssembler.toModel(produtoAtualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar um produto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Produto deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content)
    })
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        produtoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
