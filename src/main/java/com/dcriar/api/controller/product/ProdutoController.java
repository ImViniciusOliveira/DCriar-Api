package com.dcriar.api.controller.product;

import com.dcriar.api.hateous.assembler.ProdutoModelAssembler;
import com.dcriar.api.dto.request.product.ProdutoRequestDTO;
import com.dcriar.api.dto.response.product.ProdutoResponseDTO;
import com.dcriar.api.hateous.model.ProdutoModel;
import com.dcriar.domain.product.service.ProdutoService;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

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
    @Operation(summary = "Listar todos os produtos")
    @ApiResponse(responseCode = "200", description = "Lista de produtos retornada com sucesso")
    public CollectionModel<ProdutoModel> findAll() {
        List<ProdutoResponseDTO> produtos = produtoService.findAll();
        return produtoModelAssembler.toCollectionModel(produtos);
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
        ProdutoModel produtoModel = produtoModelAssembler.toModel(produtoCriado);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(produtoCriado.getId())
                .toUri();

        return ResponseEntity.created(location).body(produtoModel);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar um produto existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content)
    })
    public ProdutoModel update(@PathVariable Long id, @RequestBody @Valid ProdutoRequestDTO requestDTO) {
        ProdutoResponseDTO produtoAtualizado = produtoService.update(id, requestDTO);
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
