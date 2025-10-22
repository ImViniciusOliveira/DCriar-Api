package com.dcriar.api.hateous.product.assembler;

import com.dcriar.api.controller.product.EstoqueProdutoController;
import com.dcriar.api.controller.product.ProdutoController;
import com.dcriar.api.controller.stock.TipoMateriaPrimaController;
import com.dcriar.api.dto.response.product.ProdutoResponseDTO;
import com.dcriar.api.mapper.product.ProdutoMapper;
import com.dcriar.api.hateous.product.model.ProdutoModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Assembler responsável por converter {@link ProdutoResponseDTO} em {@link ProdutoModel},
 * adicionando links HATEOAS para navegação entre endpoints relacionados ao produto.
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
     * Converte um DTO de produto em seu modelo de representação HATEOAS.
     * <p>Adiciona links de navegação globais (como a coleção de produtos) e,
     * se o produto já existir (tiver um ID), adiciona links de auto-referência
     * e ações específicas (atualizar, deletar, etc.).
     *
     * @param dto DTO de resposta do produto
     * @return Modelo HATEOAS enriquecido
     */
    @Override
    @NonNull
    public ProdutoModel toModel(@NonNull ProdutoResponseDTO dto) {
        ProdutoModel model = mapper.toModel(dto);

        // Links de navegação e descoberta (presentes em todos os modelos de produto)
        model.add(linkTo(methodOn(ProdutoController.class).findAll()).withRel("produtos"));
        model.add(linkTo(methodOn(TipoMateriaPrimaController.class).findAll()).withRel("buscar-tipos-materia-prima"));

        // Adiciona links específicos do recurso apenas se o produto já existir (tiver um ID)
        if (dto.getId() != null) {
            // Links de auto-referência e ações
            model.add(linkTo(methodOn(ProdutoController.class).findById(dto.getId())).withSelfRel());
            model.add(linkTo(methodOn(ProdutoController.class).update(dto.getId(), new com.dcriar.api.dto.request.product.ProdutoRequestDTO())).withRel("atualizar-produto"));
            model.add(linkTo(methodOn(ProdutoController.class).patch(dto.getId(), new java.util.HashMap<>())).withRel("atualizar-parcialmente-produto"));
            model.add(linkTo(methodOn(ProdutoController.class).deleteById(dto.getId())).withRel("deletar-produto"));
            model.add(linkTo(methodOn(ProdutoController.class).uploadFoto(dto.getId(), null)).withRel("upload-foto")); // Para MultipartFile, null é aceitável

            // Links para recursos relacionados
            model.add(linkTo(methodOn(EstoqueProdutoController.class).listarEstoquesPorProduto(dto.getId())).withRel("estoques-do-produto"));
            model.add(linkTo(methodOn(EstoqueProdutoController.class).listarMovimentacoesPorProduto(dto.getId())).withRel("historico-movimentacoes"));
        }

        return model;
    }

    /**
     * Envolve uma coleção de modelos de produto em um {@link CollectionModel},
     * adicionando links de nível de coleção, como o link para criar um novo produto.
     *
     * @param dtos A coleção de {@link ProdutoResponseDTO}.
     * @return Um {@link CollectionModel} com os modelos de produto e links de coleção.
     */
    @Override
    @NonNull
    public CollectionModel<ProdutoModel> toCollectionModel(@NonNull Iterable<? extends ProdutoResponseDTO> dtos) {
        CollectionModel<ProdutoModel> collectionModel = super.toCollectionModel(dtos);
        collectionModel.add(linkTo(methodOn(ProdutoController.class).getNewProductTemplate()).withRel("novo-produto"));
        return collectionModel;
    }

    /**
     * Cria uma resposta HTTP 201 (Created) com o modelo HATEOAS do produto e o header Location.
     * O header 'Location' é construído de forma robusta a partir do link 'self' do próprio modelo.
     *
     * @param dto DTO de resposta do produto
     * @return ResponseEntity com status 201 e modelo HATEOAS no corpo
     */
    public ResponseEntity<ProdutoModel> toCreatedResponseEntity(@NonNull ProdutoResponseDTO dto) {
        ProdutoModel model = toModel(dto);

        // Usa o link 'self' do modelo para garantir que o header Location seja sempre correto.
        return model.getLink("self")
                .map(link -> ResponseEntity.created(link.toUri()).body(model))
                .orElse(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()); // Fallback, não deve ocorrer
    }

    /**
     * Cria uma resposta HTTP 200 (OK) com o modelo HATEOAS do produto. Este método
     * encapsula a lógica de conversão e montagem da resposta para o controller.
     *
     * @param dto DTO de resposta do produto
     * @return ResponseEntity com status 200 e modelo HATEOAS no corpo
     */
    public ResponseEntity<ProdutoModel> toOkResponseEntity(@NonNull ProdutoResponseDTO dto) {
        return ResponseEntity.ok(toModel(dto));
    }
}
