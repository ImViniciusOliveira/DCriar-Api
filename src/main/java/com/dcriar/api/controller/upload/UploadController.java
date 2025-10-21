package com.dcriar.api.controller.upload;

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
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Controller responsável por gerenciar o upload e download de arquivos de forma genérica.
 * <p>
 * Este controller lida com a recepção de arquivos, seu armazenamento físico e a disponibilização
 * para download. Ele não possui conhecimento sobre os contextos de negócio (como Produtos ou Usuários)
 * nos quais os arquivos são utilizados, garantindo o desacoplamento de responsabilidades.
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
     * @param file O arquivo a ser enviado.
     * @return Um ResponseEntity com status 200 OK e um corpo contendo a URL de download do arquivo.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Fazer upload de um arquivo")
    public ResponseEntity<UploadResponseDTO> uploadFile(
            @RequestParam("file") MultipartFile file) {

        String fileName = fileStorageService.storeFile(file);

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
        // Decodifica nome recebido via URL (por segurança)
        String decodedName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
        Resource resource = fileStorageService.loadFileAsResource(decodedName);

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ignored) {
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        // Content-Disposition com suporte a nomes com espaços/acentos (RFC5987)
        String filename = resource.getFilename();
        assert filename != null;
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encodedFilename;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }
}
