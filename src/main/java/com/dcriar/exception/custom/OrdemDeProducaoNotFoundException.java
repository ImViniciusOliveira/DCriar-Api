package com.dcriar.exception.custom;

import lombok.Getter;

/**
 * Exceção lançada quando uma Ordem de Produção não é encontrada no sistema.
 */
@Getter
public class OrdemDeProducaoNotFoundException extends RuntimeException {

    /**
     * O ID da ordem de produção não encontrada.
     */
    private final Long id;

    /**
     * Constrói a exceção com o ID da ordem de produção não encontrada.
     *
     * @param id O ID da ordem de produção.
     */
    public OrdemDeProducaoNotFoundException(Long id) {
        super("Ordem de Produção não encontrada com ID: " + id);
        this.id = id;
    }
}
