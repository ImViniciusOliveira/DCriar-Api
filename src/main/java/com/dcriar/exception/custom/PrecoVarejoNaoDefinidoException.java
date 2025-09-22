package com.dcriar.exception.custom;

import lombok.Getter;

/**
 * Exception lançada quando um produto não possui preço de varejo definido.
 */
@Getter
public class PrecoVarejoNaoDefinidoException extends RuntimeException {

    private final Long produtoId;

    public PrecoVarejoNaoDefinidoException(Long produtoId) {
        super("Preço de varejo não definido para o produto ID: " + produtoId);
        this.produtoId = produtoId;
    }
}
