package com.dcriar.exception.custom;

import lombok.Getter;

@Getter
public class CanalVendaNotFoundException extends RuntimeException {
    private final Long id;

    public CanalVendaNotFoundException(Long id) {
        super("Canal de venda não encontrado com o ID: " + id);
        this.id = id;
    }
}
