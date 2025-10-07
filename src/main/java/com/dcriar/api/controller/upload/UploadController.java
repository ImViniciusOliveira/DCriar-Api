package com.dcriar.api.controller.upload;

import com.dcriar.api.dto.request.upload.FileUploadRequestDTO;
import com.dcriar.api.dto.response.upload.UploadResponseDTO;
import com.dcriar.domain.upload.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;

/**
 * Controller responsável por gerenciar o upload e download de arquivos.
 * Expõe endpoints para enviar arquivos para o servidor e para recuperá-los posteriormente.
 */
@RestController
@RequestMapping("/api/v1/uploads")
@RequiredArgsConstructor
@Tag(name = "Uploads", description = "Endpoints para upload e download de arquivos")
public class UploadController {

    private final FileStorageService fileStorageService;

    /**
     * Realiza o upload de um arquivo para o servidor.
     * O arquivo é armazenado e uma URL para download é retornada.
     *
     * @param fileUploadRequestDTO DTO contendo o arquivo a ser enviado.
     * @return Um ResponseEntity com status 200 OK e um corpo contendo a URL de download do arquivo.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Fazer upload de um arquivo")
    public ResponseEntity<UploadResponseDTO> uploadFile(
            @ModelAttribute FileUploadRequestDTO fileUploadRequestDTO) {
        MultipartFile file = fileUploadRequestDTO.getFile();

        // Armazena o arquivo usando o serviço de armazenamento e obtém o nome do arquivo salvo.
        String fileName = fileStorageService.storeFile(file);

        // Constrói a URI de download para o arquivo recém-carregado.
        // Isso permite que os clientes acessem o arquivo através de um endpoint GET.
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/uploads/")
                .path(fileName)
                .toUriString();

        return ResponseEntity.ok(new UploadResponseDTO(fileDownloadUri));
    }

    /**
     * Permite o download de um arquivo previamente enviado.
     *
     * @param fileName O nome do arquivo a ser baixado.
     * @param request  A requisição HTTP, usada para determinar o tipo de conteúdo do arquivo.
     * @return Um ResponseEntity contendo o recurso do arquivo para download.
     *         O cabeçalho Content-Disposition é definido como 'attachment' para forçar o download.
     */
    @GetMapping("/{fileName:.+}")
    @Operation(summary = "Baixar um arquivo")
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "Nome do arquivo a ser baixado (ex: 123e4567-e89b-12d3-a456-426614174000_minha_imagem.jpg)", example = "123e4567-e89b-12d3-a456-426614174000_exemplo.png")
            @PathVariable String fileName, HttpServletRequest request) {
        // Carrega o arquivo como um recurso a partir do serviço de armazenamento.
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // Tenta determinar o tipo de conteúdo (MIME type) do arquivo.
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ignored) {
            // Ignora a exceção se não for possível determinar o tipo de conteúdo.
            // O fallback abaixo cuidará disso.
        }

        // Se o tipo de conteúdo não puder ser determinado, usa um tipo padrão.
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        // Constrói a resposta HTTP com o conteúdo do arquivo.
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
