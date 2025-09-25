package com.dcriar.exception.custom;

import lombok.Getter;

import java.util.Map;

@Getter
public class ProdutoInvalidoException extends RuntimeException {

    private final Map<String, String> errors;

    public ProdutoInvalidoException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors;
    }

}
