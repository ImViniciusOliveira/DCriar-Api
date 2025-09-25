package com.dcriar.domain.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Classe embutível (@Embeddable) que representa as dimensões de um item.
 * <p>
 * É usada para agrupar campos relacionados (como largura e comprimento) dentro de
 * uma entidade, mantendo o modelo de domínio organizado e a tabela do banco de dados plana.
 */
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Dimensoes {

    @Column(name = "largura_cm_unitaria", precision = 10, scale = 2)
    private BigDecimal larguraCm;

    @Column(name = "comprimento_cm_unitario", precision = 10, scale = 2)
    private BigDecimal comprimentoCm;

    public BigDecimal getComprimentoM() {
        return comprimentoCm.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
    }

    public BigDecimal getAreaCm2() {
        return larguraCm.multiply(comprimentoCm);
    }
}

