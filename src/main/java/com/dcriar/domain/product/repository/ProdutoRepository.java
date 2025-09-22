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

    // Verifica se já existe produto com o mesmo nome
    boolean existsByNome(String nome);

    // Verifica se já existe produto com o mesmo SKU
    boolean existsBySku(String sku);

    // Verifica se existe outro produto com o mesmo nome (excluindo o produto de id passado)
    boolean existsByNomeAndIdNot(String nome, Long id);

    // Verifica se existe outro produto com o mesmo SKU (excluindo o produto de id passado)
    boolean existsBySkuAndIdNot(String sku, Long id);
}
