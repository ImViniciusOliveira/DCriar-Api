package com.dcriar.exception.custom;

import lombok.Getter;
import java.util.Set;

@Getter
public class ProdutoEmUsoException extends RuntimeException {

    private final Long produtoId;
    private final Set<Long> entidadeIds;

    public ProdutoEmUsoException(Long produtoId, Set<Long> ordemDeCorteIds) {
        super("Produto com id " + produtoId + " está em uso nas ordens de corte: " + ordemDeCorteIds + " e não pode ser excluído.");
        this.produtoId = produtoId;
        this.entidadeIds = ordemDeCorteIds;
    }
}