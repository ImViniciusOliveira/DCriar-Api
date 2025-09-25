package com.dcriar.domain.production.repository;

import com.dcriar.domain.production.entity.OrdemDeCorte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdemDeCorteRepository extends JpaRepository<OrdemDeCorte, Long> {
    boolean existsByProdutoId(Long produtoId);

}
