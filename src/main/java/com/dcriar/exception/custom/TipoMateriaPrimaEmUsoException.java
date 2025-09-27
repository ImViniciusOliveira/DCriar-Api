package com.dcriar.exception.custom;

import lombok.Getter;
import java.util.Set;

@Getter
public class TipoMateriaPrimaEmUsoException extends RuntimeException {

    private final Long tipoMateriaPrimaId;
    private final Set<Long> loteIds;

    public TipoMateriaPrimaEmUsoException(Long tipoMateriaPrimaId, Set<Long> loteIds) {
        super("O tipo de matéria-prima com id " + tipoMateriaPrimaId + " está em uso nos lotes: " + loteIds + " e não pode ser excluído.");
        this.tipoMateriaPrimaId = tipoMateriaPrimaId;
        this.loteIds = loteIds;
    }
}