package com.dcriar.domain.production.repository;

import com.dcriar.domain.product.entity.Produto;
import com.dcriar.domain.production.entity.OrdemDeCorte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdemDeCorteRepository extends JpaRepository<OrdemDeCorte, Long> {

    boolean existsByProduto(Produto produto);

    List<OrdemDeCorte> findAllByProduto(Produto produto);

}
