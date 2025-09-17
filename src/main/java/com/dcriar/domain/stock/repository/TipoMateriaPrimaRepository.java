package com.dcriar.domain.stock.repository;

import com.dcriar.domain.stock.entity.TipoMateriaPrima;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório para a entidade {@link TipoMateriaPrima}.
 * <p>
 * Fornece os métodos de acesso a dados para o catálogo de tipos de matérias-primas,
 * utilizando a abstração do Spring Data JPA.
 */
@Repository
public interface TipoMateriaPrimaRepository extends JpaRepository<TipoMateriaPrima, Long> {
}
