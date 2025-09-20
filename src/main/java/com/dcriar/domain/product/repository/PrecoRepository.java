package com.dcriar.domain.product.repository;

import com.dcriar.domain.product.entity.Preco;
import com.dcriar.domain.product.entity.Produto;
import com.dcriar.domain.product.entity.enuns.TipoPreco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositório para a entidade Preco.
 */
@Repository
public interface PrecoRepository extends JpaRepository<Preco, Long> {

    /**
     * Busca um registro de preço pela combinação de produto e tipo de preço.
     * O Spring Data JPA cria a implementação deste método automaticamente
     * com base no seu nome.
     *
     * @param produto O produto a ser buscado.
     * @param tipoPreco O tipo de preço (VAREJO, REVENDA).
     * @return Um Optional contendo o preço, se encontrado.
     */
    Optional<Preco> findByProdutoAndTipoPreco(Produto produto, TipoPreco tipoPreco);
}

