package com.dcriar.domain.stock.entity;

import com.dcriar.domain.stock.entity.enuns.UnidadeDeMedida;
import jakarta.persistence.*;
import lombok.*;

/**
 * Representa um tipo abstrato de matéria-prima no sistema (o item de catálogo).
 * <p>
 * Esta entidade define as características gerais de um insumo, como seu nome
 * e a unidade em que ele é consumido nas "receitas" dos produtos.
 */
@Entity
@Table(name = "tipos_materia_prima")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@ToString
@EqualsAndHashCode(of = "id")
public class TipoMateriaPrima {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * O nome único do tipo de matéria-prima, servindo como uma chave de negócio.
     */
    @Column(nullable = false, unique = true, length = 150)
    private String nome;

    /**
     * Define como a "receita" de um produto mede o consumo deste material.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UnidadeDeMedida unidadeDeConsumo;
}

