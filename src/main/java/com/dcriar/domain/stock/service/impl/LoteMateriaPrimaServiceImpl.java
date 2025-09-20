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
import com.dcriar.domain.stock.entity.enuns.TipoMovimentacao;
import com.dcriar.domain.stock.entity.enuns.UnidadeDeMedida;
import com.dcriar.domain.stock.repository.LoteMateriaPrimaRepository;
import com.dcriar.domain.stock.repository.MovimentacaoEstoqueLoteRepository;
import com.dcriar.domain.stock.repository.TipoMateriaPrimaRepository;
import com.dcriar.domain.stock.service.LoteMateriaPrimaService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementação da lógica de negócio para gerenciamento de Lotes de Matéria-Prima.
 */
@Service
@RequiredArgsConstructor
public class LoteMateriaPrimaServiceImpl implements LoteMateriaPrimaService {

    private final LoteMateriaPrimaRepository loteMateriaPrimaRepository;
    private final TipoMateriaPrimaRepository tipoMateriaPrimaRepository;
    private final MovimentacaoEstoqueLoteRepository movimentacaoEstoqueLoteRepository;
    private final LoteMateriaPrimaMapper loteMateriaPrimaMapper;
    private final MovimentacaoMapper movimentacaoMapper;

    @Override
    @Transactional
    public LoteMateriaPrimaResponseDTO create(LoteMateriaPrimaRequestDTO requestDTO) {
        TipoMateriaPrima tipoMateriaPrima = tipoMateriaPrimaRepository.findById(requestDTO.getTipoMateriaPrimaId())
                .orElseThrow(() -> new EntityNotFoundException("Tipo de Matéria-Prima não encontrado com o ID: " + requestDTO.getTipoMateriaPrimaId()));

        LoteMateriaPrima novoLote = LoteMateriaPrima.builder()
                .tipoMateriaPrima(tipoMateriaPrima)
                .unidadeDeEstoque(requestDTO.getUnidadeDeEstoque())
                .atributos(requestDTO.getAtributos())
                .build();

        // --- INÍCIO DA NOVA LÓGICA DE CUSTEIO ---
        BigDecimal custoPorUnidadeBase = calcularCustoPorUnidadeBase(requestDTO, tipoMateriaPrima);
        // --- FIM DA NOVA LÓGICA DE CUSTEIO ---

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
     * Calcula o custo por unidade base de um lote no momento da sua entrada.
     * Ex: Calcula o custo por cm² de um rolo de adesivo.
     */
    private BigDecimal calcularCustoPorUnidadeBase(LoteMateriaPrimaRequestDTO dto, TipoMateriaPrima tipo) {
        if (dto.getCustoTotalLote() == null) {
            return null;
        }

        BigDecimal totalUnidadesBase;
        UnidadeDeMedida unidadeConsumo = tipo.getUnidadeDeConsumo();

        switch (dto.getUnidadeDeEstoque()) {
            case METRO_LINEAR:
                if (unidadeConsumo != UnidadeDeMedida.CENTIMETRO_QUADRADO) {
                    throw new IllegalStateException("Cálculo de custo para METRO_LINEAR só é suportado com consumo em CENTIMETRO_QUADRADO.");
                }
                Object larguraMmObj = dto.getAtributos().get("larguraMm");
                if (!(larguraMmObj instanceof Number)) {
                    throw new IllegalStateException("Para lotes em METRO_LINEAR, o atributo 'larguraMm' é obrigatório para o cálculo de custo.");
                }
                BigDecimal larguraCm = new BigDecimal(((Number) larguraMmObj).intValue()).divide(new BigDecimal("10"));
                BigDecimal comprimentoCm = dto.getQuantidadeInicial().multiply(new BigDecimal("100"));
                totalUnidadesBase = larguraCm.multiply(comprimentoCm); // Área Total em cm²
                break;
            case LITRO:
                if (unidadeConsumo != UnidadeDeMedida.MILILITRO) {
                    throw new IllegalStateException("Cálculo de custo para LITRO só é suportado com consumo em MILILITRO.");
                }
                totalUnidadesBase = dto.getQuantidadeInicial().multiply(new BigDecimal("1000"));
                break;
            case UNIDADE:
            case GRAMA:
            default:
                totalUnidadesBase = dto.getQuantidadeInicial();
                break;
        }

        if (totalUnidadesBase.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("A quantidade total de unidades base para cálculo de custo deve ser maior que zero.");
        }

        // Custo por Unidade Base = Custo Total do Lote / Total de Unidades Base
        return dto.getCustoTotalLote().divide(totalUnidadesBase, 8, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional(readOnly = true)
    public LoteMateriaPrimaResponseDTO findById(Long id) {
        LoteMateriaPrima lote = findLoteById(id);
        BigDecimal saldo = calcularSaldo(lote);

        LoteMateriaPrimaResponseDTO responseDTO = loteMateriaPrimaMapper.toResponseDTO(lote);
        responseDTO.setSaldoEstoque(saldo);

        return responseDTO;
    }

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

    @Override
    @Transactional
    public MovimentacaoResponseDTO registrarMovimentacao(Long loteId, MovimentacaoRequestDTO requestDTO) {
        LoteMateriaPrima lote = findLoteById(loteId);

        if (requestDTO.getQuantidade().compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal saldoAtual = calcularSaldo(lote);
            if (saldoAtual.add(requestDTO.getQuantidade()).compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Saldo de estoque insuficiente para esta operação.");
            }
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

    @Override
    @Transactional(readOnly = true)
    public List<MovimentacaoResponseDTO> listarMovimentacoesPorLote(Long loteId) {
        LoteMateriaPrima lote = findLoteById(loteId);
        return movimentacaoEstoqueLoteRepository.findAllByLote(lote).stream()
                .map(movimentacaoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    private LoteMateriaPrima findLoteById(Long id) {
        return loteMateriaPrimaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lote de Matéria-Prima não encontrado com o ID: " + id));
    }

    private BigDecimal calcularSaldo(LoteMateriaPrima lote) {
        return movimentacaoEstoqueLoteRepository.findSaldoByLote(lote);
    }
}

