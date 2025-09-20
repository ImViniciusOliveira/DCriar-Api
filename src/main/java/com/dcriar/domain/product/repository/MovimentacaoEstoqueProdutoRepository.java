package com.dcriar.domain.product.repository;

import com.dcriar.domain.product.entity.MovimentacaoEstoqueProduto;
import com.dcriar.domain.product.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório para a entidade MovimentacaoEstoqueProduto.
 */
@Repository
public interface MovimentacaoEstoqueProdutoRepository extends JpaRepository<MovimentacaoEstoqueProduto, Long> {

    /**
     * Calcula o saldo de estoque físico total para um determinado produto
     * somando todas as suas movimentações.
     */
    @Query("SELECT COALESCE(SUM(m.quantidade), 0) FROM MovimentacaoEstoqueProduto m WHERE m.produto = :produto")
    Integer findSaldoByProduto(@Param("produto") Produto produto);

    /**
     * Busca todo o histórico de movimentações ("Livro-Razão") de um produto específico.
     * O Spring Data JPA cria a implementação deste método automaticamente
     * com base no seu nome, gerando uma query "WHERE produto = ?".
     *
     * @param produto O produto cujo histórico será buscado.
     * @return Uma lista com todas as movimentações do produto.
     */
    List<MovimentacaoEstoqueProduto> findAllByProduto(Produto produto);
}

