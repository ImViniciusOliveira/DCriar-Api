package com.dcriar.exception.custom;

import lombok.Getter;

/**
 * Exceção lançada quando uma operação de produção é tentada em um produto
 * que não é compatível com o método de produção especificado (ex: tentar cortar um produto de consumo direto).
 */
@Getter
public class TipoDeProducaoIncompativelException extends RuntimeException {

    /**
     * O ID do produto que causou o erro.
     */
    private final Long produtoId;

    /**
     * Constrói a exceção com a mensagem de erro.
     *
     * @param produtoId O ID do produto.
     * @param message   A mensagem explicando a incompatibilidade.
     */
    public TipoDeProducaoIncompativelException(Long produtoId, String message) {
        super(message);
        this.produtoId = produtoId;
    }
}
