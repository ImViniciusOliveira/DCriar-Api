package com.dcriar.exception.custom;

import lombok.Getter;

@Getter
public class ProdutoNotFoundException extends RuntimeException {
    private final Long id;

    public ProdutoNotFoundException(Long id) {
        super("Produto não encontrado com o ID: " + id);
        this.id = id;
    }

}
