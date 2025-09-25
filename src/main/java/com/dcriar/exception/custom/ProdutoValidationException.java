package com.dcriar.exception.custom;

import java.util.Map;

/**
 * Exception personalizada para validações de Produto.
 * Permite capturar múltiplos erros de campos.
 */
public class ProdutoValidationException extends RuntimeException {

    private final Map<String, String> errors;

    public ProdutoValidationException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
