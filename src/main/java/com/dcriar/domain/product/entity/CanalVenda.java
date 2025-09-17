package com.dcriar.domain.product.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Representa um canal de venda onde o estoque de produtos acabados é gerido.
 * <p>
 * Exemplos: Loja Física, Shopee, Mercado Livre.
 */
@Entity
@Table(name = "canais_venda")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = "id")
public class CanalVenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nome;
}
