package com.dcriar.api.controller;

import com.dcriar.api.controller.product.ProdutoController;
import com.dcriar.api.controller.sales.VendaController;
import com.dcriar.api.controller.stock.LoteMateriaPrimaController;
import com.dcriar.api.controller.production.OrdemDeProducaoController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Controller para o ponto de entrada (root) da API.
 * <p>
 * Este endpoint ({@code /api/v1}) serve como o "portão de entrada" para a API,
 * seguindo os princípios HATEOAS. Ele retorna os links para os principais
 * recursos (coleções), permitindo que os clientes descubram dinamicamente
 * as URLs disponíveis sem a necessidade de "chumbá-las" no código.
 */
@RestController
@RequestMapping("/api/v1")
public class ApiRootController {

    @GetMapping
    @Operation(summary = "Ponto de entrada da API", description = "Retorna os links para os principais recursos da API.")
    @ApiResponse(responseCode = "200", description = "Links de recursos retornados com sucesso.")
    public RepresentationModel<?> getRoot() {
        RepresentationModel<?> rootModel = new RepresentationModel<>();

        // Construímos manualmente um link templated para produtos, incluindo os parâmetros de paginação
        // Usa o método overload sem parâmetros (existente apenas para link building) para evitar passar nulls
        String produtosBase = linkTo(methodOn(ProdutoController.class).findAll()).toUri().toString();
        // Adiciona o link simples (compatibilidade com front antigo) e um link templated para clientes que desejam paginação
        rootModel.add(Link.of(produtosBase).withRel("produtos"));
        rootModel.add(Link.of(produtosBase + "{?page,size,sort}").withRel("produtos-paged"));

        rootModel.add(linkTo(methodOn(LoteMateriaPrimaController.class).findAll()).withRel("lotes-materia-prima"));
        rootModel.add(linkTo(methodOn(VendaController.class).findAll()).withRel("vendas"));
        rootModel.add(linkTo(methodOn(OrdemDeProducaoController.class).listarTodas()).withRel("ordens-de-producao"));

        return rootModel;
    }
}
