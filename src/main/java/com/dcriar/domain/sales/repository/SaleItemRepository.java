package com.dcriar.domain.sales.repository;

import com.dcriar.domain.sales.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório para a entidade SaleItem (Item de Venda).
 * <p>
 * Fornece as operações de CRUD (Criar, Ler, Atualizar, Deletar) básicas
 * para os itens de venda, através da abstração do Spring Data JPA.
 */
@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {
}
