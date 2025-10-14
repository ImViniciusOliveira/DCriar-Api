package com.dcriar.api.hateous.product.assembler;

import com.dcriar.api.controller.product.EstoqueProdutoController;
import com.dcriar.api.controller.product.ProdutoController;
import com.dcriar.api.controller.stock.TipoMateriaPrimaController;
import com.dcriar.api.dto.response.product.ProdutoResponseDTO;
import com.dcriar.api.mapper.product.ProdutoMapper;
import com.dcriar.api.hateous.product.model.ProdutoModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Assembler responsável por converter {@link ProdutoResponseDTO} em {@link ProdutoModel},
 * adicionando links HATEOAS para navegação entre endpoints relacionados ao produto.
 * <p>
 * Utilizado nas respostas das controllers para enriquecer os modelos retornados com links de auto, edição,
 * exclusão, navegação e recursos relacionados (estoque, movimentações, tipos de matéria-prima).
 * <p>
 * Exemplo de uso:
 * <pre>
 *   ProdutoModel model = produtoModelAssembler.toModel(produtoResponseDTO);
 * </pre>
 */
@Component
public class ProdutoModelAssembler extends RepresentationModelAssemblerSupport<ProdutoResponseDTO, ProdutoModel> {

    private final ProdutoMapper mapper;

    /**
     * Cria o assembler com o mapper de Produto.
     * @param mapper Mapper para conversão de DTO para Model
     */
    public ProdutoModelAssembler(ProdutoMapper mapper) {
        super(ProdutoController.class, ProdutoModel.class);
        this.mapper = mapper;
    }

    /**
     * Converte um {@link ProdutoResponseDTO} em {@link ProdutoModel},
     * adicionando links HATEOAS para operações e recursos relacionados.
     * <p>
     * Links adicionados:
     * <ul>
     *   <li>Auto (self)</li>
     *   <li>Atualizar produto</li>
     *   <li>Deletar produto</li>
     *   <li>Listar todos os produtos</li>
     *   <li>Buscar tipos de matéria-prima</li>
     *   <li>Estoques do produto</li>
     *   <li>Histórico de movimentações</li>
     * </ul>
     * @param dto DTO de resposta do produto
     * @return Modelo HATEOAS enriquecido
     */
    @Override
    @NonNull
    public ProdutoModel toModel(@NonNull ProdutoResponseDTO dto) {
        ProdutoModel model = mapper.toModel(dto);

        // Links do próprio recurso
        model.add(linkTo(methodOn(ProdutoController.class).findById(dto.getId())).withSelfRel());
        model.add(linkTo(methodOn(ProdutoController.class).update(dto.getId(), null)).withRel("atualizar-produto"));
        model.add(linkTo(methodOn(ProdutoController.class).deleteById(dto.getId())).withRel("deletar-produto"));

        // Links de navegação e descoberta
        model.add(linkTo(methodOn(ProdutoController.class).findAll()).withRel("produtos"));
        model.add(linkTo(methodOn(TipoMateriaPrimaController.class).findAll(null, null, null, null)).withRel("buscar-tipos-materia-prima"));

        // Links para recursos relacionados
        model.add(linkTo(methodOn(EstoqueProdutoController.class).listarEstoquesPorProduto(dto.getId())).withRel("estoques-do-produto"));
        model.add(linkTo(methodOn(EstoqueProdutoController.class).listarMovimentacoesPorProduto(dto.getId())).withRel("historico-movimentacoes"));

        return model;
    }

    /**
     * Cria uma resposta HTTP 201 (Created) com o modelo HATEOAS do produto e o header Location.
     * <p>
     * Exemplo de uso:
     * <pre>
     *   ResponseEntity<ProdutoModel> response = produtoModelAssembler.toCreatedResponseEntity(dto);
     * </pre>
     * @param dto DTO de resposta do produto
     * @return ResponseEntity com status 201 e modelo HATEOAS no corpo
     */
    public ResponseEntity<ProdutoModel> toCreatedResponseEntity(@NonNull ProdutoResponseDTO dto) {
        ProdutoModel model = toModel(dto);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(dto.getId())
                .toUri();

        return ResponseEntity.created(location).body(model);
    }
}
