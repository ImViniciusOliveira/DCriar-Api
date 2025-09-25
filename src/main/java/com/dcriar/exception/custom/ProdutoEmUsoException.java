package com.dcriar.exception.custom;

public class ProdutoEmUsoException extends RuntimeException {
    public ProdutoEmUsoException(Long produtoId) {
        super("Produto com id " + produtoId + " não pode ser excluído pois está sendo usado em ordens de corte.");
    }
}
