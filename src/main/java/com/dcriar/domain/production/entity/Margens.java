package com.dcriar.domain.production.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Margens {

    @Column(name = "margem_superior_cm", precision = 10, scale = 2)
    private BigDecimal superior;

    @Column(name = "margem_inferior_cm", precision = 10, scale = 2)
    private BigDecimal inferior;

    @Column(name = "margem_esquerda_cm", precision = 10, scale = 2)
    private BigDecimal esquerda;

    @Column(name = "margem_direita_cm", precision = 10, scale = 2)
    private BigDecimal direita;
}
