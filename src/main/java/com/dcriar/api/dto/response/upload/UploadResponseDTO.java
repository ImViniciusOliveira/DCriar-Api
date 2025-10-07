package com.dcriar.api.dto.response.upload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Data Transfer Object) que representa a resposta de uma operação de upload de arquivo.
 * Contém a URL para download do arquivo que foi carregado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponseDTO {

    /**
     * A URL completa a partir da qual o arquivo pode ser baixado.
     */
    private String fileDownloadUri;
}
