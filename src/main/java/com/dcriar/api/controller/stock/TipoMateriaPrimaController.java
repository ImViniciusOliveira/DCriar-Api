package com.dcriar.api.controller.stock;

import com.dcriar.api.dto.request.stock.TipoMateriaPrimaRequestDTO;
import com.dcriar.api.dto.response.stock.TipoMateriaPrimaResponseDTO;
import com.dcriar.domain.stock.service.TipoMateriaPrimaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Controller REST para o gerenciamento de Tipos de Matéria-Prima.
 * <p>
 * Expõe os endpoints para as operações de CRUD (Criar, Ler, Atualizar, Deletar)
 * relacionadas aos tipos de insumos cadastrados no sistema.
 */
@RestController
@RequestMapping("/api/v1/tipos-materia-prima")
@RequiredArgsConstructor
@Tag(name = "Tipos de Matéria-Prima", description = "Endpoints para o catálogo de insumos")
public class TipoMateriaPrimaController {

    private final TipoMateriaPrimaService tipoMateriaPrimaService;

    @GetMapping
    @Operation(summary = "Listar todos os tipos de matéria-prima", description = "Retorna uma lista de todos os tipos de matéria-prima cadastrados.")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TipoMateriaPrimaResponseDTO.class))))
    public ResponseEntity<List<TipoMateriaPrimaResponseDTO>> findAll() {
        return ResponseEntity.ok(tipoMateriaPrimaService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar tipo de matéria-prima por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Tipo não encontrado", content = @Content)
    })
    public ResponseEntity<TipoMateriaPrimaResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(tipoMateriaPrimaService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Criar um novo tipo de matéria-prima")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tipo criado com sucesso",
                    headers = @Header(name = "Location", description = "URL do novo recurso")),
            @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos", content = @Content)
    })
    public ResponseEntity<TipoMateriaPrimaResponseDTO> create(@RequestBody @Valid TipoMateriaPrimaRequestDTO requestDTO) {
        TipoMateriaPrimaResponseDTO tipoCriado = tipoMateriaPrimaService.create(requestDTO);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(tipoCriado.id())
                .toUri();

        return ResponseEntity.created(location).body(tipoCriado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar um tipo de matéria-prima existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Tipo não encontrado", content = @Content)
    })
    public ResponseEntity<TipoMateriaPrimaResponseDTO> update(@PathVariable Long id, @RequestBody @Valid TipoMateriaPrimaRequestDTO requestDTO) {
        return ResponseEntity.ok(tipoMateriaPrimaService.update(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar um tipo de matéria-prima")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Tipo deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Tipo não encontrado", content = @Content)
    })
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        tipoMateriaPrimaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
