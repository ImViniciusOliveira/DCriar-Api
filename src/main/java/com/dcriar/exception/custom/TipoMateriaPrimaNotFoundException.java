package com.dcriar.exception.custom;

import lombok.Getter;

@Getter
public class TipoMateriaPrimaNotFoundException extends RuntimeException {
    private final Long materiaPrimaId;

    public TipoMateriaPrimaNotFoundException(Long materiaPrimaId) {
        super("Tipo de Matéria-Prima não encontrado com o ID: " + materiaPrimaId);
        this.materiaPrimaId = materiaPrimaId;
    }
}
