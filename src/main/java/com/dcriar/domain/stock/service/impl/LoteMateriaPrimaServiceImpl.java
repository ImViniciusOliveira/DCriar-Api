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
import com.dcriar.domain.stock.repository.specification.LoteMateriaPrimaSpecification;
import com.dcriar.domain.stock.repository.MovimentacaoEstoqueLoteRepository;
import com.dcriar.domain.stock.repository.TipoMateriaPrimaRepository;
import com.dcriar.domain.stock.service.LoteMateriaPrimaService;
import com.dcriar.exception.custom.EstoqueInsuficienteParaMovimentacaoException;
import com.dcriar.exception.custom.EstoqueRegraNegocioException;
import com.dcriar.exception.custom.LoteMateriaPrimaNotFoundException;
import com.dcriar.exception.custom.TipoMateriaPrimaNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementação da lógica de negócio para o gerenciamento de Lotes de Matéria-Prima.
 * <p>
 * Esta classe é responsável por todas as operações de CRUD e regras de negócio
 * relacionadas aos lotes de matéria-prima, como a criação, busca, registro de movimentações
 * e cálculo de saldos, garantindo a consistência dos dados.
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
     * Cria um novo lote de matéria-prima no sistema.
     * <p>
     * Associa o lote a um tipo de matéria-prima existente e registra uma movimentação
     * inicial de entrada (compra) com o custo por unidade base calculado.
     *
     * @param requestDTO O DTO com os dados para a criação do lote.
     * @return O {@link LoteMateriaPrimaResponseDTO} do lote recém-criado com seu saldo inicial.
     * @throws TipoMateriaPrimaNotFoundException se o tipo de matéria-prima especificado não for encontrado.
     * @throws EstoqueRegraNegocioException se houver erros nas regras de negócio durante o cálculo do custo.
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
     * Este método é utilizado para determinar o custo unitário do material
     * com base na unidade de consumo definida para o tipo de matéria-prima.
     *
     * @param dto O DTO de requisição do lote de matéria-prima, contendo o custo total e a quantidade inicial.
     * @param tipo O tipo de matéria-prima associado ao lote.
     * @return O custo por unidade base como um {@link BigDecimal}.
     * @throws EstoqueRegraNegocioException se houver inconsistências nos atributos ou unidades de medida para o cálculo.
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
     * Busca um lote de matéria-prima específico pelo seu ID.
     * Calcula e enriquece o DTO de resposta com o saldo de estoque atual.
     *
     * @param id O ID do lote a ser buscado.
     * @return O {@link LoteMateriaPrimaResponseDTO} do lote encontrado com seu saldo.
     * @throws LoteMateriaPrimaNotFoundException se o lote com o ID especificado não for encontrado.
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
     * Lista todos os lotes de matéria-prima, com filtros opcionais.
     * Para cada lote, calcula e enriquece o DTO de resposta com o saldo de estoque atual.
     *
     * @param tipoMateriaPrimaId O ID do tipo de matéria-prima para filtrar (opcional).
     * @param apenasLotesPrincipais Se true, filtra apenas lotes que não são sobras (loteDeOrigemId é nulo) (opcional).
     * @return Uma lista de {@link LoteMateriaPrimaResponseDTO} contendo os lotes encontrados com seus saldos.
     */
    @Override
    @Transactional(readOnly = true)
    public List<LoteMateriaPrimaResponseDTO> findAll(Long tipoMateriaPrimaId, Boolean apenasLotesPrincipais) {
        Specification<LoteMateriaPrima> spec = LoteMateriaPrimaSpecification.comFiltros(tipoMateriaPrimaId, apenasLotesPrincipais);

        return loteMateriaPrimaRepository.findAll(spec).stream()
                .map(lote -> {
                    BigDecimal saldo = calcularSaldo(lote);
                    LoteMateriaPrimaResponseDTO dto = loteMateriaPrimaMapper.toResponseDTO(lote);
                    dto.setSaldoEstoque(saldo);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Registra uma nova movimentação de estoque para um lote de matéria-prima.
     * <p>
     * Pode ser uma entrada ou saída. Valida se há saldo suficiente para movimentações de saída.
     *
     * @param loteId O ID do lote de matéria-prima.
     * @param requestDTO O DTO com os dados da movimentação (tipo, quantidade, motivo).
     * @return O {@link MovimentacaoResponseDTO} da movimentação registrada.
     * @throws LoteMateriaPrimaNotFoundException se o lote com o ID especificado não for encontrado.
     * @throws EstoqueInsuficienteParaMovimentacaoException se não houver saldo suficiente para uma movimentação de saída.
     */
    @Override
    @Transactional
    public MovimentacaoResponseDTO registrarMovimentacao(Long loteId, MovimentacaoRequestDTO requestDTO) {
        LoteMateriaPrima lote = findLoteById(loteId);
        BigDecimal saldoAtual = calcularSaldo(lote);

        if (requestDTO.getQuantidade().compareTo(BigDecimal.ZERO) < 0 &&
                saldoAtual.add(requestDTO.getQuantidade()).compareTo(BigDecimal.ZERO) < 0) {
            throw new EstoqueInsuficienteParaMovimentacaoException(
                    lote.getId(),
                    requestDTO.getQuantidade().abs().doubleValue(),
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
     * Lista todo o histórico de movimentações de um lote de matéria-prima específico.
     *
     * @param loteId O ID do lote cujo histórico será consultado.
     * @return Uma lista de {@link MovimentacaoResponseDTO} representando todas as movimentações do lote.
     * @throws LoteMateriaPrimaNotFoundException se o lote com o ID especificado não for encontrado.
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
     * Busca uma entidade {@link LoteMateriaPrima} pelo seu ID.
     * Método auxiliar para evitar duplicação de código e centralizar o tratamento de "não encontrado".
     *
     * @param id O ID do lote a ser buscado.
     * @return A entidade {@link LoteMateriaPrima} encontrada.
     * @throws LoteMateriaPrimaNotFoundException se o lote com o ID especificado não for encontrado.
     */
    private LoteMateriaPrima findLoteById(Long id) {
        return loteMateriaPrimaRepository.findById(id)
                .orElseThrow(() -> new LoteMateriaPrimaNotFoundException(id));
    }

    /**
     * Calcula o saldo de estoque atual para um determinado lote de matéria-prima.
     * <p>
     * A soma das quantidades de todas as movimentações associadas ao lote.
     *
     * @param lote O lote para o qual o saldo será calculado.
     * @return O saldo de estoque atual como um {@link BigDecimal}.
     */
    private BigDecimal calcularSaldo(LoteMateriaPrima lote) {
        return movimentacaoEstoqueLoteRepository.findSaldoByLote(lote);
    }
}
