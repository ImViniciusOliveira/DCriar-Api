package com.dcriar.api.dto.request.upload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO utilizado para encapsular o arquivo enviado em requisições de upload.
 * Ajuda o Swagger UI a renderizar corretamente o campo de upload de arquivo.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileUploadRequestDTO {

    @Schema(description = "O arquivo a ser enviado.")
    private MultipartFile file;
}
