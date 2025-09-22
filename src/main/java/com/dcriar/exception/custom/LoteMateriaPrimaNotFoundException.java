package com.dcriar.exception.custom;

import lombok.Getter;

@Getter
public class LoteMateriaPrimaNotFoundException extends RuntimeException {
    private final Long id;

    public LoteMateriaPrimaNotFoundException(Long id) {
        super("Lote de matéria-prima não encontrado com o ID: " + id);
        this.id = id;
    }
}
