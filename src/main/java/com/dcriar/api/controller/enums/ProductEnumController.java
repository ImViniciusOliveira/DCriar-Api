package com.dcriar.api.controller.enums;

import com.dcriar.api.hateous.enums.assembler.TipoMovimentacaoProdutoModelAssembler;
import com.dcriar.api.hateous.enums.assembler.TipoPrecoModelAssembler;
import com.dcriar.api.hateous.enums.model.TipoMovimentacaoProdutoModel;
import com.dcriar.api.hateous.enums.model.TipoPrecoModel;
import com.dcriar.domain.product.entity.enums.TipoMovimentacaoProduto;
import com.dcriar.domain.product.entity.enums.TipoPreco;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/**
 * Controlador para expor os enums relacionados ao domínio de Produto.
 * <p>
 * A injeção de dependência é feita via campo (@Autowired) para quebrar uma dependência circular
 * de compilação com os assemblers, que precisam da referência da classe do controller.
 */
@RestController
@RequestMapping("/v1/enums/product")
@Tag(name = "Enums - Produto", description = "Endpoints para consulta de enums relacionados a produtos")
public class ProductEnumController {

    private final TipoPrecoModelAssembler tipoPrecoModelAssembler;

    private final TipoMovimentacaoProdutoModelAssembler tipoMovimentacaoProdutoModelAssembler;

    public ProductEnumController(TipoPrecoModelAssembler tipoPrecoModelAssembler, TipoMovimentacaoProdutoModelAssembler tipoMovimentacaoProdutoModelAssembler) {
        this.tipoPrecoModelAssembler = tipoPrecoModelAssembler;
        this.tipoMovimentacaoProdutoModelAssembler = tipoMovimentacaoProdutoModelAssembler;
    }

    /**
     * Retorna uma coleção de todos os tipos de preço disponíveis, com links HATEOAS.
     *
     * @return Um {@link ResponseEntity} com um {@link CollectionModel} de {@link TipoPrecoModel}.
     */
    @GetMapping("/tipos-preco")
    @Operation(summary = "Listar todos os Tipos de Preço")
    @ApiResponse(responseCode = "200", description = "Lista de tipos de preço retornada com sucesso")
    public ResponseEntity<CollectionModel<TipoPrecoModel>> getTiposPreco() {
        return ResponseEntity.ok(tipoPrecoModelAssembler.toCollectionModel(Arrays.asList(TipoPreco.values())));
    }

    /**
     * Retorna uma coleção de todos os tipos de movimentação de produto disponíveis, com links HATEOAS.
     *
     * @return Um {@link ResponseEntity} com um {@link CollectionModel} de {@link TipoMovimentacaoProdutoModel}.
     */
    @GetMapping("/tipos-movimentacao-produto")
    @Operation(summary = "Listar todos os Tipos de Movimentação de Produto")
    @ApiResponse(responseCode = "200", description = "Lista de tipos de movimentação de produto retornada com sucesso")
    public ResponseEntity<CollectionModel<TipoMovimentacaoProdutoModel>> getTiposMovimentacaoProduto() {
        return ResponseEntity.ok(tipoMovimentacaoProdutoModelAssembler.toCollectionModel(Arrays.asList(TipoMovimentacaoProduto.values())));
    }
}
