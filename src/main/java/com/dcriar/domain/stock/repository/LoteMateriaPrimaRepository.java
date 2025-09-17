package com.dcriar.domain.stock.repository;

import com.dcriar.domain.stock.entity.LoteMateriaPrima;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório para a entidade {@link LoteMateriaPrima}.
 * <p>
 * Fornece os métodos de acesso a dados para os lotes físicos de matérias-primas
 * em estoque, utilizando a abstração do Spring Data JPA.
 */
@Repository
public interface LoteMateriaPrimaRepository extends JpaRepository<LoteMateriaPrima, Long> {
}
