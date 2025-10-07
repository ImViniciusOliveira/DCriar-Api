package com.dcriar.api.dto.request.upload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO utilizado para encapsular o arquivo enviado em requisições de upload.
 * Ajuda o Swagger UI a renderizar corretamente o campo de upload de arquivo.
 */
@Data
public class FileUploadRequestDTO {

    @Schema(description = "O arquivo a ser enviado.")
    private MultipartFile file;
}
