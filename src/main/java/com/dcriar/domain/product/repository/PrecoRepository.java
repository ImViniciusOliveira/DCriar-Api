package com.dcriar.domain.product.repository;

import com.dcriar.domain.product.entity.Preco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório para a entidade Preco.
 * <p>
 * Fornece as operações de CRUD (Criar, Ler, Atualizar, Deletar) básicas
 * para os preços dos produtos, através da abstração do Spring Data JPA.
 */
@Repository
public interface PrecoRepository extends JpaRepository<Preco, Long> {
}
