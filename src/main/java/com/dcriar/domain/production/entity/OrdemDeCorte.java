package com.dcriar.domain.production.entity;

import com.dcriar.domain.product.entity.Produto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@ToString
@EqualsAndHashCode(of = "id")
@Table(name = "ordens_de_corte")
public class OrdemDeCorte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(name = "lote_principal_id", nullable = false)
    private Long lotePrincipalId;

    @Column(name = "quantidade_produzida", nullable = false)
    private Integer quantidadeProduzida;

    @Column(name = "modo_calculo", nullable = false)
    private String modoCalculo;

    @Column(name = "largura_final_cm", nullable = false)
    private Double larguraFinalCm;

    @Column(name = "comprimento_final_cm", nullable = false)
    private Double comprimentoFinalCm;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "motivo")
    private String motivo;
}