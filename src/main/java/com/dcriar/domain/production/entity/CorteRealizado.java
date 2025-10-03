package com.dcriar.domain.production.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "cortes_realizados")
public class CorteRealizado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordem_de_corte_id", nullable = false)
    private OrdemDeCorte ordemDeCorte;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal larguraCm;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal comprimentoCm;

    @Column(nullable = false)
    private int quantidade;

    @Column(nullable = false, length = 20)
    private String tipo; // "PRODUTO" ou "RETALHO"
}
