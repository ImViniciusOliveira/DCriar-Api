package com.dcriar.exception.custom;

import lombok.Getter;

import java.util.Map;

/**
 * Exceção para validações de múltiplos campos ou regras de negócio.
 * Permite retornar vários erros em um único objeto.
 */
@Getter
public class MultiValidationException extends RuntimeException {

    private final Map<String, String> errors;

    public MultiValidationException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors;
    }

}
