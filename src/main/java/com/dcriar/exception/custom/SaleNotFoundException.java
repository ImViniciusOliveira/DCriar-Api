package com.dcriar.exception.custom;

import lombok.Getter;

/**
 * Exception lançada quando uma venda não é encontrada no banco de dados.
 */
@Getter
public class SaleNotFoundException extends RuntimeException {

    private final Long saleId;

    public SaleNotFoundException(Long saleId) {
        super("Venda não encontrada com o ID: " + saleId);
        this.saleId = saleId;
    }
}
