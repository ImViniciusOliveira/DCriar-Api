package com.dcriar.domain.production.service.impl;

import com.dcriar.api.dto.request.production.OrdemDeCorteRequestDTO;
import com.dcriar.api.dto.request.product.AjusteEstoqueRequestDTO;
import com.dcriar.domain.product.entity.MovimentacaoEstoqueProduto;
import com.dcriar.domain.product.entity.Produto;
import com.dcriar.domain.product.entity.enuns.TipoMovimentacaoProduto;
import com.dcriar.domain.stock.entity.LoteMateriaPrima;
import com.dcriar.domain.stock.entity.MovimentacaoEstoqueLote;
import com.dcriar.domain.stock.entity.enuns.TipoMovimentacao;
import com.dcriar.domain.stock.entity.enuns.UnidadeDeMedida;
import com.dcriar.domain.production.service.OrdemDeProducaoService;
import com.dcriar.domain.product.repository.MovimentacaoEstoqueProdutoRepository;
import com.dcriar.domain.product.repository.ProdutoRepository;
import com.dcriar.domain.product.service.EstoqueProdutoService;
import com.dcriar.domain.stock.repository.LoteMateriaPrimaRepository;
import com.dcriar.domain.stock.repository.MovimentacaoEstoqueLoteRepository;
import com.dcriar.exception.custom.LoteMateriaPrimaNotFoundException;
import com.dcriar.exception.custom.ProdutoNotFoundException;
import com.dcriar.exception.custom.RegraNegocioException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Implementação da lógica de negócio para Ordens de Produção.
 */
@Service
@RequiredArgsConstructor
public class OrdemDeProducaoServiceImpl implements OrdemDeProducaoService {

    private final LoteMateriaPrimaRepository loteMateriaPrimaRepository;
    private final MovimentacaoEstoqueLoteRepository movimentacaoEstoqueLoteRepository;
    private final ProdutoRepository produtoRepository;
    private final MovimentacaoEstoqueProdutoRepository movimentacaoEstoqueProdutoRepository;
    private final EstoqueProdutoService estoqueProdutoService;

    @Override
    @Transactional
    public void processarOrdemDeCorte(OrdemDeCorteRequestDTO requestDTO) {
        LoteMateriaPrima lotePrincipal = findLoteById(requestDTO.getLotePrincipalId());

        // 1. Validações de Negócio
        validarOrdemDeCorte(lotePrincipal, requestDTO);

        // 2. Cálculo do consumo e da sobra
        BigDecimal larguraTotalCm = getLarguraEmCm(lotePrincipal.getAtributos());
        BigDecimal comprimentoDeCorteMetros = requestDTO.getComprimentoDeCorteCm()
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal larguraSobraCm = larguraTotalCm.subtract(requestDTO.getLarguraDeCorteCm());

        // 3. Registra a saída no lote principal
        registrarSaida(lotePrincipal, comprimentoDeCorteMetros, requestDTO.getMotivo());

        // 4. Cria o novo lote de retalho, se houver sobra
        if (larguraSobraCm.compareTo(BigDecimal.ZERO) > 0) {
            criarLoteDeRetalho(lotePrincipal, larguraSobraCm, comprimentoDeCorteMetros);
        }

        // 5. Registra a entrada do produto acabado
        registrarEntradaProdutoAcabado(requestDTO);

        // 6. Aloca o novo estoque ao canal de venda de destino
        distribuirEstoqueParaCanal(requestDTO);
    }

    private void registrarEntradaProdutoAcabado(OrdemDeCorteRequestDTO requestDTO) {
        Produto produto = produtoRepository.findById(requestDTO.getProdutoId())
                .orElseThrow(() -> new ProdutoNotFoundException(requestDTO.getProdutoId()));

        MovimentacaoEstoqueProduto entradaProducao = MovimentacaoEstoqueProduto.builder()
                .produto(produto)
                .tipo(TipoMovimentacaoProduto.ENTRADA_PRODUCAO)
                .quantidade(requestDTO.getQuantidadeProduzida())
                .motivo("Produzido via Ordem de Corte. Consumiu o Lote ID: " + requestDTO.getLotePrincipalId())
                .build();

        movimentacaoEstoqueProdutoRepository.save(entradaProducao);
    }

    private void distribuirEstoqueParaCanal(OrdemDeCorteRequestDTO requestDTO) {
        AjusteEstoqueRequestDTO ajusteDTO = AjusteEstoqueRequestDTO.builder()
                .produtoId(requestDTO.getProdutoId())
                .canalVendaId(requestDTO.getCanalVendaDestinoId())
                .quantidade(requestDTO.getQuantidadeProduzida())
                .build();

        estoqueProdutoService.ajustarEstoque(ajusteDTO);
    }

    private void validarOrdemDeCorte(LoteMateriaPrima lote, OrdemDeCorteRequestDTO dto) {
        if (lote.getUnidadeDeEstoque() != UnidadeDeMedida.METRO_LINEAR) {
            throw new RegraNegocioException("Ordens de corte só podem ser processadas em lotes com unidade METRO_LINEAR.");
        }

        BigDecimal larguraTotalCm = getLarguraEmCm(lote.getAtributos());
        if (dto.getLarguraDeCorteCm().compareTo(larguraTotalCm) > 0) {
            throw new RegraNegocioException("A largura de corte não pode ser maior que a largura total do lote.");
        }

        BigDecimal saldoAtualMetros = movimentacaoEstoqueLoteRepository.findSaldoByLote(lote);
        BigDecimal comprimentoDeCorteMetros = dto.getComprimentoDeCorteCm()
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

        if (comprimentoDeCorteMetros.compareTo(saldoAtualMetros) > 0) {
            throw new RegraNegocioException("Saldo de estoque insuficiente para este corte.");
        }
    }

    private void registrarSaida(LoteMateriaPrima lote, BigDecimal comprimentoDeCorteMetros, String motivo) {
        MovimentacaoEstoqueLote saida = MovimentacaoEstoqueLote.builder()
                .lote(lote)
                .tipo(TipoMovimentacao.SAIDA_PRODUCAO)
                .quantidade(comprimentoDeCorteMetros.negate())
                .motivo(motivo)
                .build();

        movimentacaoEstoqueLoteRepository.save(saida);
    }

    private void criarLoteDeRetalho(LoteMateriaPrima lotePrincipal, BigDecimal larguraSobraCm, BigDecimal comprimentoMetros) {
        Map<String, Object> novosAtributos = Map.of(
                "larguraMm", larguraSobraCm.multiply(new BigDecimal("10")).intValue()
        );

        LoteMateriaPrima loteRetalho = LoteMateriaPrima.builder()
                .tipoMateriaPrima(lotePrincipal.getTipoMateriaPrima())
                .unidadeDeEstoque(UnidadeDeMedida.METRO_LINEAR)
                .atributos(novosAtributos)
                .loteDeOrigem(lotePrincipal)
                .build();

        MovimentacaoEstoqueLote entradaRetalho = MovimentacaoEstoqueLote.builder()
                .lote(loteRetalho)
                .tipo(TipoMovimentacao.ENTRADA_SOBRA)
                .quantidade(comprimentoMetros)
                .motivo("Retalho gerado a partir do Lote ID: " + lotePrincipal.getId())
                .build();

        loteRetalho.getMovimentacoes().add(entradaRetalho);
        loteMateriaPrimaRepository.save(loteRetalho);
    }

    private LoteMateriaPrima findLoteById(Long id) {
        return loteMateriaPrimaRepository.findById(id)
                .orElseThrow(() -> new LoteMateriaPrimaNotFoundException(id));
    }

    private BigDecimal getLarguraEmCm(Map<String, Object> atributos) {
        Object larguraMmObj = atributos.get("larguraMm");
        if (!(larguraMmObj instanceof Number)) {
            throw new RegraNegocioException("O lote não possui o atributo 'larguraMm' numérico para realizar o cálculo de corte.");
        }
        return new BigDecimal(((Number) larguraMmObj).intValue())
                .divide(new BigDecimal("10"), 2, RoundingMode.HALF_UP);
    }
}
