package com.dcriar.exception.custom;

import lombok.Getter;

@Getter
public class TipoMateriaPrimaAlreadyExistsException extends RuntimeException {

    private final String nome;

    public TipoMateriaPrimaAlreadyExistsException(String nome) {
        super("Tipo de matéria-prima já existente: " + nome);
        this.nome = nome;
    }
}
