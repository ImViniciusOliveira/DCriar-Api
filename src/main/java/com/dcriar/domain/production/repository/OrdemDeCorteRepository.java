package com.dcriar.domain.production.repository;

import com.dcriar.domain.product.entity.Produto;
import com.dcriar.domain.production.entity.OrdemDeCorte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório para a entidade {@link OrdemDeCorte}.
 * <p>
 * Fornece os métodos de acesso a dados para as ordens de corte,
 * utilizando a abstração do Spring Data JPA.
 */
@Repository
public interface OrdemDeCorteRepository extends JpaRepository<OrdemDeCorte, Long> {

    /**
     * Busca todas as ordens de corte associadas a um determinado produto.
     *
     * @param produto O produto cujas ordens de corte serão buscadas.
     * @return Uma lista com todas as ordens de corte encontradas para o produto.
     */
    List<OrdemDeCorte> findAllByProduto(Produto produto);

}
