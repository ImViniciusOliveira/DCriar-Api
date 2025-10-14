package com.dcriar.api.controller.sales;

import com.dcriar.api.dto.request.sales.SaleRequestDTO;
import com.dcriar.api.dto.response.sales.SaleResponseDTO;
import com.dcriar.api.hateous.sales.assembler.SaleModelAssembler;
import com.dcriar.api.hateous.sales.model.SaleModel;
import com.dcriar.domain.sales.service.SaleService;
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
    private final SaleModelAssembler saleModelAssembler;

    /**
     * Registra uma nova venda e orquestra a baixa automática de estoque.
     * <p>
     * Exemplo de uso: POST /api/v1/sales
     *
     * @param requestDTO Dados da venda
     * @return Venda registrada com links HATEOAS e header Location
     */
    @PostMapping
    @Operation(summary = "Registar uma nova venda")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Venda registada com sucesso.",
                    headers = @Header(name = "Location", description = "URL do novo recurso")),
            @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos ou estoque insuficiente.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto ou Canal de Venda não encontrado.", content = @Content)
    })
    public ResponseEntity<SaleModel> registerSale(@RequestBody @Valid SaleRequestDTO requestDTO) {
        SaleResponseDTO registeredSale = saleService.registerSale(requestDTO);
        return saleModelAssembler.toCreatedResponseEntity(registeredSale);
    }

    /**
     * Lista todas as vendas registradas no sistema.
     * <p>
     * Exemplo de uso: GET /api/v1/sales
     *
     * @return Lista de vendas com links HATEOAS
     */
    @GetMapping
    @Operation(summary = "Listar todas as vendas")
    @ApiResponse(responseCode = "200", description = "Lista de vendas retornada com sucesso.")
    public ResponseEntity<CollectionModel<SaleModel>> findAll() {
        List<SaleResponseDTO> sales = saleService.findAll();
        return ResponseEntity.ok(saleModelAssembler.toCollectionModel(sales));
    }

    /**
     * Busca os detalhes de uma venda específica pelo seu ID.
     * <p>
     * Exemplo de uso: GET /api/v1/sales/{id}
     *
     * @param id ID da venda
     * @return Venda encontrada com links HATEOAS
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar uma venda por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Venda encontrada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Venda não encontrada.", content = @Content)
    })
    public ResponseEntity<SaleModel> findById(@PathVariable Long id) {
        SaleResponseDTO sale = saleService.findById(id);
        return ResponseEntity.ok(saleModelAssembler.toModel(sale));
    }
}
