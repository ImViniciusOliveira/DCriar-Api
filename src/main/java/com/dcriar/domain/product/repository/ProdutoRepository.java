package com.dcriar.domain.product.repository;

import com.dcriar.domain.product.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Interface de repositório para a entidade {@link Produto}.
 * <p>
 * Provê métodos de acesso a dados (CRUD) para produtos, abstraindo a complexidade
 * da camada de persistência.
 */
@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
}
