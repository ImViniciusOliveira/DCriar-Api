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

    /**
     * Verifica se já existe um produto com o nome especificado.
     *
     * @param nome O nome do produto a ser verificado.
     * @return {@code true} se um produto com o nome existir, {@code false} caso contrário.
     */
    boolean existsByNome(String nome);

    /**
     * Verifica se já existe um produto com o SKU (Stock Keeping Unit) especificado.
     *
     * @param sku O SKU do produto a ser verificado.
     * @return {@code true} se um produto com o SKU existir, {@code false} caso contrário.
     */
    boolean existsBySku(String sku);

    /**
     * Verifica se existe outro produto com o nome especificado, excluindo o produto com o ID fornecido.
     * Útil para validações de atualização onde o próprio produto pode manter seu nome.
     *
     * @param nome O nome do produto a ser verificado.
     * @param id O ID do produto a ser excluído da verificação.
     * @return {@code true} se outro produto com o nome existir, {@code false} caso contrário.
     */
    boolean existsByNomeAndIdNot(String nome, Long id);

    /**
     * Verifica se existe outro produto com o SKU (Stock Keeping Unit) especificado, excluindo o produto com o ID fornecido.
     * Útil para validações de atualização onde o próprio produto pode manter seu SKU.
     *
     * @param sku O SKU do produto a ser verificado.
     * @param id O ID do produto a ser excluído da verificação.
     * @return {@code true} se outro produto com o SKU existir, {@code false} caso contrário.
     */
    boolean existsBySkuAndIdNot(String sku, Long id);
}
