package com.dcriar.domain.stock.service;

import com.dcriar.api.dto.request.stock.LoteMateriaPrimaRequestDTO;
import com.dcriar.api.dto.request.stock.MovimentacaoRequestDTO;
import com.dcriar.api.dto.response.stock.LoteMateriaPrimaResponseDTO;
import com.dcriar.api.dto.response.stock.MovimentacaoResponseDTO;

import java.util.List;

/**
 * Interface que define o contrato para a lógica de negócio de Lotes de Matéria-Prima.
 */
public interface LoteMateriaPrimaService {

    /**
     * Dá entrada de um novo lote de matéria-prima no estoque.
     */
    LoteMateriaPrimaResponseDTO create(LoteMateriaPrimaRequestDTO requestDTO);

    /**
     * Busca um lote de matéria-prima pelo seu ID.
     */
    LoteMateriaPrimaResponseDTO findById(Long id);

    /**
     * Lista todos os lotes de matéria-prima cadastrados no sistema.
     */
    List<LoteMateriaPrimaResponseDTO> findAll();

    /**
     * Regista uma nova movimentação de estoque para um lote existente.
     * <p>
     * Esta operação é usada para dar baixa no estoque (SAIDA_PRODUCAO),
     * registar perdas (PERDA_DESCARTE) ou fazer correções (AJUSTE_INVENTARIO).
     *
     * @param loteId O ID do lote a ser movimentado.
     * @param requestDTO O DTO com os detalhes da movimentação.
     * @return O DTO de resposta da movimentação recém-criada.
     */
    MovimentacaoResponseDTO registrarMovimentacao(Long loteId, MovimentacaoRequestDTO requestDTO);

    /**
     * Lista todo o histórico de movimentações ("Livro-Razão") de um lote específico.
     *
     * @param loteId O ID do lote cujo histórico será consultado.
     * @return Uma lista de DTOs, cada um representando uma movimentação.
     */
    List<MovimentacaoResponseDTO> listarMovimentacoesPorLote(Long loteId);
}

