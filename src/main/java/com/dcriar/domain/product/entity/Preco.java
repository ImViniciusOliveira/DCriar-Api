package com.dcriar.domain.product.entity;

import com.dcriar.domain.product.entity.enums.TipoPreco;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Representa o preço de um produto para um determinado tipo de precificação.
 * <p>
 * Permite que um único produto tenha múltiplos preços, como um para varejo
 * e outro para revenda.
 */
@Entity
@Table(name = "precos")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = "id")
public class Preco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_preco", nullable = false, length = 50)
    private TipoPreco tipoPreco;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Column(name = "valor_promocional", precision = 19, scale = 2)
    private BigDecimal valorPromocional;

    @Column(name = "promocao_ativa", nullable = false)
    private boolean promocaoAtiva;
}
