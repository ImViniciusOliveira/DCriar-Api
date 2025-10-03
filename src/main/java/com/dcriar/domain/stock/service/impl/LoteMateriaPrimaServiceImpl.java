package com.dcriar.domain.stock.service.impl;

import com.dcriar.api.dto.request.stock.LoteMateriaPrimaRequestDTO;
import com.dcriar.api.dto.request.stock.MovimentacaoRequestDTO;
import com.dcriar.api.dto.response.stock.LoteMateriaPrimaResponseDTO;
import com.dcriar.api.dto.response.stock.MovimentacaoResponseDTO;
import com.dcriar.api.mapper.stock.LoteMateriaPrimaMapper;
import com.dcriar.api.mapper.stock.MovimentacaoMapper;
import com.dcriar.domain.stock.entity.LoteMateriaPrima;
import com.dcriar.domain.stock.entity.MovimentacaoEstoqueLote;
import com.dcriar.domain.stock.entity.TipoMateriaPrima;
import com.dcriar.domain.stock.entity.enums.TipoMovimentacao;
import com.dcriar.domain.stock.entity.enums.UnidadeDeMedida;
import com.dcriar.domain.stock.repository.LoteMateriaPrimaRepository;
import com.dcriar.domain.stock.repository.MovimentacaoEstoqueLoteRepository;
import com.dcriar.domain.stock.repository.TipoMateriaPrimaRepository;
import com.dcriar.domain.stock.service.LoteMateriaPrimaService;
import com.dcriar.exception.custom.EstoqueInsuficienteParaMovimentacaoException;
import com.dcriar.exception.custom.EstoqueRegraNegocioException;
import com.dcriar.exception.custom.LoteMateriaPrimaNotFoundException;
import com.dcriar.exception.custom.TipoMateriaPrimaNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementação da lógica de negócio para o gerenciamento de Lotes de Matéria-Prima.
 * <p>
 * Esta classe coordena as operações de criação, busca e movimentação de lotes,
 * garantindo a consistência dos dados e aplicando as regras de negócio.
 */
@Service
@RequiredArgsConstructor
public class LoteMateriaPrimaServiceImpl implements LoteMateriaPrimaService {

    private final LoteMateriaPrimaRepository loteMateriaPrimaRepository;
    private final TipoMateriaPrimaRepository tipoMateriaPrimaRepository;
    private final MovimentacaoEstoqueLoteRepository movimentacaoEstoqueLoteRepository;
    private final LoteMateriaPrimaMapper loteMateriaPrimaMapper;
    private final MovimentacaoMapper movimentacaoMapper;

    /**
     * Cria um novo lote de matéria-prima no estoque e registra sua movimentação inicial de compra.
     * <p>
     * O processo inclui:
     * <ol>
     *     <li>Busca e validação do tipo de matéria-prima.</li>
     *     <li>Cálculo do custo por unidade base do lote.</li>
     *     <li>Criação do lote e sua movimentação inicial de ENTRADA_COMPRA.</li>
     *     <li>Persistência do lote e da movimentação.</li>
     * </ol>
     *
     * @param requestDTO O DTO contendo os dados para a criação do lote.
     * @return Um {@link LoteMateriaPrimaResponseDTO} representando o lote recém-criado com seu saldo inicial.
     * @throws TipoMateriaPrimaNotFoundException se o tipo de matéria-prima não for encontrado.
     * @throws EstoqueRegraNegocioException se houver um erro nas regras de negócio durante o cálculo do custo por unidade base.
     */
    @Override
    @Transactional
    public LoteMateriaPrimaResponseDTO create(LoteMateriaPrimaRequestDTO requestDTO) {
        TipoMateriaPrima tipoMateriaPrima = tipoMateriaPrimaRepository.findById(requestDTO.getTipoMateriaPrimaId())
                .orElseThrow(() -> new TipoMateriaPrimaNotFoundException(requestDTO.getTipoMateriaPrimaId()));

        LoteMateriaPrima novoLote = LoteMateriaPrima.builder()
                .tipoMateriaPrima(tipoMateriaPrima)
                .unidadeDeEstoque(requestDTO.getUnidadeDeEstoque())
                .atributos(requestDTO.getAtributos())
                .build();

        BigDecimal custoPorUnidadeBase = calcularCustoPorUnidadeBase(requestDTO, tipoMateriaPrima);

        MovimentacaoEstoqueLote movimentacaoInicial = MovimentacaoEstoqueLote.builder()
                .lote(novoLote)
                .tipo(TipoMovimentacao.ENTRADA_COMPRA)
                .quantidade(requestDTO.getQuantidadeInicial())
                .motivo(requestDTO.getMotivo() != null ? requestDTO.getMotivo() : "Entrada inicial do lote no sistema.")
                .custoPorUnidadeBase(custoPorUnidadeBase)
                .build();

        novoLote.getMovimentacoes().add(movimentacaoInicial);
        LoteMateriaPrima loteSalvo = loteMateriaPrimaRepository.save(novoLote);

        LoteMateriaPrimaResponseDTO responseDTO = loteMateriaPrimaMapper.toResponseDTO(loteSalvo);
        responseDTO.setSaldoEstoque(requestDTO.getQuantidadeInicial());

        return responseDTO;
    }

    /**
     * Calcula o custo por unidade base de um lote de matéria-prima.
     * <p>
     * Este cálculo é crucial para a valoração do estoque e depende da unidade de estoque
     * do lote e da unidade de consumo do tipo de matéria-prima.
     *
     * @param dto O DTO de requisição do lote de matéria-prima.
     * @param tipo O tipo de matéria-prima associado ao lote.
     * @return O custo por unidade base como {@link BigDecimal}.
     * @throws EstoqueRegraNegocioException se as unidades de medida forem incompatíveis para o cálculo
     *                                      ou se atributos necessários (ex: larguraMm) estiverem ausentes/inválidos.
     */
    private BigDecimal calcularCustoPorUnidadeBase(LoteMateriaPrimaRequestDTO dto, TipoMateriaPrima tipo) {
        if (dto.getCustoTotalLote() == null) {
            return null;
        }

        BigDecimal totalUnidadesBase;
        UnidadeDeMedida unidadeConsumo = tipo.getUnidadeDeConsumo();

        switch (dto.getUnidadeDeEstoque()) {
            case METRO_LINEAR -> {
                if (unidadeConsumo != UnidadeDeMedida.CENTIMETRO_QUADRADO) {
                    throw new EstoqueRegraNegocioException(String.format(
                            "Cálculo de custo para %s só é suportado com consumo em %s.",
                            UnidadeDeMedida.METRO_LINEAR.getDescricao(), UnidadeDeMedida.CENTIMETRO_QUADRADO.getDescricao()));
                }
                Object larguraMmObj = dto.getAtributos().get("larguraMm");
                if (!(larguraMmObj instanceof Number)) {
                    throw new EstoqueRegraNegocioException(String.format(
                            "Para lotes em %s, o atributo 'larguraMm' é obrigatório e deve ser um número para o cálculo de custo.",
                            UnidadeDeMedida.METRO_LINEAR.getDescricao()));
                }
                BigDecimal larguraCm = new BigDecimal(((Number) larguraMmObj).intValue())
                        .divide(new BigDecimal("10"), 2, RoundingMode.HALF_UP);
                BigDecimal comprimentoCm = dto.getQuantidadeInicial().multiply(new BigDecimal("100"));
                totalUnidadesBase = larguraCm.multiply(comprimentoCm);
            }
            case LITRO -> {
                if (unidadeConsumo != UnidadeDeMedida.MILILITRO) {
                    throw new EstoqueRegraNegocioException(String.format(
                            "Cálculo de custo para %s só é suportado com consumo em %s.",
                            UnidadeDeMedida.LITRO.getDescricao(), UnidadeDeMedida.MILILITRO.getDescricao()));
                }
                totalUnidadesBase = dto.getQuantidadeInicial().multiply(new BigDecimal("1000"));
            }
            default -> totalUnidadesBase = dto.getQuantidadeInicial();
        }

        if (totalUnidadesBase.compareTo(BigDecimal.ZERO) <= 0) {
            throw new EstoqueRegraNegocioException("A quantidade total de unidades base para cálculo de custo deve ser maior que zero.");
        }

        return dto.getCustoTotalLote().divide(totalUnidadesBase, 8, RoundingMode.HALF_UP);
    }

    /**
     * Busca um lote de matéria-prima pelo seu ID e retorna seus detalhes, incluindo o saldo atual.
     *
     * @param id O ID do lote a ser buscado.
     * @return Um {@link LoteMateriaPrimaResponseDTO} com os detalhes do lote e seu saldo.
     * @throws LoteMateriaPrimaNotFoundException se o lote não for encontrado.
     */
    @Override
    @Transactional(readOnly = true)
    public LoteMateriaPrimaResponseDTO findById(Long id) {
        LoteMateriaPrima lote = findLoteById(id);
        BigDecimal saldo = calcularSaldo(lote);

        LoteMateriaPrimaResponseDTO responseDTO = loteMateriaPrimaMapper.toResponseDTO(lote);
        responseDTO.setSaldoEstoque(saldo);

        return responseDTO;
    }

    /**
     * Lista todos os lotes de matéria-prima cadastrados no sistema, incluindo o saldo atual de cada um.
     *
     * @return Uma lista de {@link LoteMateriaPrimaResponseDTO} com todos os lotes e seus saldos.
     */
    @Override
    @Transactional(readOnly = true)
    public List<LoteMateriaPrimaResponseDTO> findAll() {
        return loteMateriaPrimaRepository.findAll().stream()
                .map(lote -> {
                    BigDecimal saldo = calcularSaldo(lote);
                    LoteMateriaPrimaResponseDTO dto = loteMateriaPrimaMapper.toResponseDTO(lote);
                    dto.setSaldoEstoque(saldo);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Registra uma nova movimentação de estoque para um lote existente.
     * <p>
     * Esta operação é usada para dar baixa no estoque (SAIDA_PRODUCAO),
     * registrar perdas (PERDA_DESCARTE) ou fazer correções (AJUSTE_INVENTARIO).
     * Antes de registrar a movimentação, verifica se há saldo suficiente para operações de saída.
     *
     * @param loteId O ID do lote a ser movimentado.
     * @param requestDTO O DTO com os detalhes da movimentação (tipo, quantidade, motivo).
     * @return O {@link MovimentacaoResponseDTO} da movimentação recém-criada.
     * @throws LoteMateriaPrimaNotFoundException se o lote não for encontrado.
     * @throws EstoqueInsuficienteParaMovimentacaoException se não houver saldo suficiente para a movimentação de saída.
     */
    @Override
    @Transactional
    public MovimentacaoResponseDTO registrarMovimentacao(Long loteId, MovimentacaoRequestDTO requestDTO) {
        LoteMateriaPrima lote = findLoteById(loteId);
        BigDecimal saldoAtual = calcularSaldo(lote);

        // Verificação de saldo para operações que diminuem o estoque
        if (requestDTO.getQuantidade().compareTo(BigDecimal.ZERO) < 0 &&
                saldoAtual.add(requestDTO.getQuantidade()).compareTo(BigDecimal.ZERO) < 0) {
            throw new EstoqueInsuficienteParaMovimentacaoException(
                    lote.getId(),
                    requestDTO.getQuantidade().abs().doubleValue(), // Quantidade requisitada sempre positiva na mensagem
                    saldoAtual.doubleValue()
            );
        }

        MovimentacaoEstoqueLote novaMovimentacao = MovimentacaoEstoqueLote.builder()
                .lote(lote)
                .tipo(requestDTO.getTipo())
                .quantidade(requestDTO.getQuantidade())
                .motivo(requestDTO.getMotivo())
                .build();

        MovimentacaoEstoqueLote movimentacaoSalva = movimentacaoEstoqueLoteRepository.save(novaMovimentacao);

        return movimentacaoMapper.toResponseDTO(movimentacaoSalva);
    }

    /**
     * Lista todo o histórico de movimentações ("Livro-Razão") de um lote específico.
     *
     * @param loteId O ID do lote cujo histórico será consultado.
     * @return Uma lista de {@link MovimentacaoResponseDTO} contendo todas as movimentações do lote.
     * @throws LoteMateriaPrimaNotFoundException se o lote não for encontrado.
     */
    @Override
    @Transactional(readOnly = true)
    public List<MovimentacaoResponseDTO> listarMovimentacoesPorLote(Long loteId) {
        LoteMateriaPrima lote = findLoteById(loteId);
        return movimentacaoEstoqueLoteRepository.findAllByLote(lote).stream()
                .map(movimentacaoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca um lote de matéria-prima pelo seu ID.
     *
     * @param id O ID do lote a ser buscado.
     * @return A entidade {@link LoteMateriaPrima} encontrada.
     * @throws LoteMateriaPrimaNotFoundException se o lote não for encontrado.
     */
    private LoteMateriaPrima findLoteById(Long id) {
        return loteMateriaPrimaRepository.findById(id)
                .orElseThrow(() -> new LoteMateriaPrimaNotFoundException(id));
    }

    /**
     * Calcula o saldo de estoque atual para um determinado lote.
     *
     * @param lote O lote para o qual o saldo será calculado.
     * @return O saldo de estoque atual como um {@link BigDecimal}.
     */
    private BigDecimal calcularSaldo(LoteMateriaPrima lote) {
        return movimentacaoEstoqueLoteRepository.findSaldoByLote(lote);
    }
}
