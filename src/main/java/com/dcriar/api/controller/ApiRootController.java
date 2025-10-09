package com.dcriar.api.controller;

import com.dcriar.api.controller.product.ProdutoController;
import com.dcriar.api.controller.sales.SaleController;
import com.dcriar.api.controller.stock.LoteMateriaPrimaController;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1")
public class ApiRootController {

    @GetMapping
    public RepresentationModel<?> getRoot() {
        RepresentationModel<?> rootModel = new RepresentationModel<>();

        rootModel.add(linkTo(methodOn(ProdutoController.class).findAll( null, null)).withRel("produtos"));
        rootModel.add(linkTo(methodOn(SaleController.class).findAll()).withRel("sales"));
        rootModel.add(linkTo(methodOn(LoteMateriaPrimaController.class).findAll(null, null)).withRel("stock"));

        return rootModel;
    }
}
