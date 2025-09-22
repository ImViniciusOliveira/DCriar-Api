package com.dcriar.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * DTO padronizado para respostas de erro da API DCriar.
 * Contém informações estruturadas para facilitar consumo por clientes da API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDTO {

    /** Timestamp do momento em que o erro ocorreu */
    private Instant timestamp;

    /** Código HTTP do erro */
    private int status;

    /** Descrição do status HTTP (ex: "Not Found", "Bad Request") */
    private String error;

    /** Mensagem amigável para o usuário ou cliente */
    private String message;

    /** Detalhes opcionais, como erros de validação ou múltiplos erros de negócio */
    private Map<String, String> details;

    /** Construtor sem detalhes */
    public ErrorDTO(Instant timestamp, int status, String error, String message) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.details = null;
    }
}
