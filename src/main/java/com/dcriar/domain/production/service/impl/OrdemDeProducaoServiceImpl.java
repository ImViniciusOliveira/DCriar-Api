package com.dcriar.domain.production.service.impl;

import com.dcriar.api.dto.request.production.OrdemDeCorteRequestDTO;
import com.dcriar.api.dto.request.product.AjusteEstoqueRequestDTO;
import com.dcriar.domain.product.entity.Dimensoes;
import com.dcriar.domain.product.entity.MovimentacaoEstoqueProduto;
import com.dcriar.domain.product.entity.Produto;
import com.dcriar.domain.product.entity.enums.TipoMovimentacaoProduto;
import com.dcriar.domain.production.enums.ModoCalculo;
import com.dcriar.domain.production.service.OrdemDeProducaoService;
import com.dcriar.domain.product.repository.MovimentacaoEstoqueProdutoRepository;
import com.dcriar.domain.product.repository.ProdutoRepository;
import com.dcriar.domain.product.service.EstoqueProdutoService;
import com.dcriar.domain.stock.entity.LoteMateriaPrima;
import com.dcriar.domain.stock.entity.MovimentacaoEstoqueLote;
import com.dcriar.domain.stock.entity.enums.TipoMovimentacao;
import com.dcriar.domain.stock.entity.enums.UnidadeDeMedida;
import com.dcriar.domain.stock.repository.LoteMateriaPrimaRepository;
import com.dcriar.domain.stock.repository.MovimentacaoEstoqueLoteRepository;
import com.dcriar.domain.production.entity.OrdemDeCorte;
import com.dcriar.domain.production.repository.OrdemDeCorteRepository;
import com.dcriar.exception.custom.LoteMateriaPrimaNotFoundException;
import com.dcriar.exception.custom.ProdutoNotFoundException;
import com.dcriar.exception.custom.RegraNegocioException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrdemDeProducaoServiceImpl implements OrdemDeProducaoService {

    private final LoteMateriaPrimaRepository loteMateriaPrimaRepository;
    private final MovimentacaoEstoqueLoteRepository movimentacaoEstoqueLoteRepository;
    private final ProdutoRepository produtoRepository;
    private final MovimentacaoEstoqueProdutoRepository movimentacaoEstoqueProdutoRepository;
    private final EstoqueProdutoService estoqueProdutoService;
    private final OrdemDeCorteRepository ordemDeCorteRepository;

    @Override
    @Transactional
    public void processarOrdemDeCorte(OrdemDeCorteRequestDTO requestDTO) {
        LoteMateriaPrima lotePrincipal = findLoteById(requestDTO.getLotePrincipalId());
        Produto produto = findProdutoById(requestDTO.getProdutoId());

        // 1. Determina as dimensões finais do corte
        Dimensoes dimensoesFinais = calcularDimensoesFinais(requestDTO, produto);

        // 2. Validações de Negócio
        validarOrdemDeCorte(lotePrincipal, dimensoesFinais);

        // 3. Cálculo do consumo e sobra
        BigDecimal larguraTotalLoteCm = getLarguraEmCm(lotePrincipal.getAtributos());
        BigDecimal comprimentoDeCorteMetros = dimensoesFinais.getComprimentoCm()
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal larguraSobraCm = larguraTotalLoteCm.subtract(dimensoesFinais.getLarguraCm());

        // 4. Cria a Ordem de Corte no banco usando builder
        OrdemDeCorte ordem = OrdemDeCorte.builder()
                .produto(produto)
                .quantidade(requestDTO.getQuantidadeProduzida())
                .dataCriacao(LocalDateTime.now())
                .build();
        ordemDeCorteRepository.save(ordem);

        // 5. Registra a saída do lote principal
        registrarSaida(lotePrincipal, comprimentoDeCorteMetros, requestDTO.getMotivo());

        // 6. Cria o lote de retalho, se houver sobra
        if (larguraSobraCm.compareTo(BigDecimal.ZERO) > 0) {
            criarLoteDeRetalho(lotePrincipal, larguraSobraCm, comprimentoDeCorteMetros);
        }

        // 7. Registra entrada do produto acabado
        registrarEntradaProdutoAcabado(requestDTO, produto);

        // 8. Ajusta estoque para o canal
        distribuirEstoqueParaCanal(requestDTO);
    }


    private Dimensoes calcularDimensoesFinais(OrdemDeCorteRequestDTO requestDTO, Produto produto) {
        if (requestDTO.getModoCalculo() == ModoCalculo.MANUAL) {
            if (requestDTO.getTamanhoFinal() == null
                    || requestDTO.getTamanhoFinal().getLarguraCm() == null
                    || requestDTO.getTamanhoFinal().getComprimentoCm() == null) {
                throw new RegraNegocioException("Para o modo MANUAL, as dimensões finais são obrigatórias.");
            }
            return new Dimensoes(
                    requestDTO.getTamanhoFinal().getLarguraCm(),
                    requestDTO.getTamanhoFinal().getComprimentoCm()
            );
        }

        if (produto.getDimensoesUnitarias() == null) {
            throw new RegraNegocioException("Produto não possui dimensões unitárias para cálculo automático.");
        }

        BigDecimal larguraUnitaria = produto.getDimensoesUnitarias().getLarguraCm();
        BigDecimal comprimentoUnitario = produto.getDimensoesUnitarias().getComprimentoCm();
        BigDecimal quantidade = new BigDecimal(requestDTO.getQuantidadeProduzida());

        BigDecimal larguraFinal = larguraUnitaria;
        BigDecimal comprimentoFinal = comprimentoUnitario.multiply(quantidade);

        if (requestDTO.getMargens() != null) {
            larguraFinal = larguraFinal
                    .add(Optional.ofNullable(requestDTO.getMargens().getEsquerda()).orElse(BigDecimal.ZERO))
                    .add(Optional.ofNullable(requestDTO.getMargens().getDireita()).orElse(BigDecimal.ZERO));
            comprimentoFinal = comprimentoFinal
                    .add(Optional.ofNullable(requestDTO.getMargens().getSuperior()).orElse(BigDecimal.ZERO))
                    .add(Optional.ofNullable(requestDTO.getMargens().getInferior()).orElse(BigDecimal.ZERO));
        }

        return new Dimensoes(larguraFinal, comprimentoFinal);
    }

    private void validarOrdemDeCorte(LoteMateriaPrima lote, Dimensoes dimensoesFinais) {
        if (lote.getUnidadeDeEstoque() != UnidadeDeMedida.METRO_LINEAR) {
            throw new RegraNegocioException("Ordens de corte só podem ser processadas em lotes METRO_LINEAR.");
        }

        BigDecimal larguraTotalLoteCm = getLarguraEmCm(lote.getAtributos());
        if (dimensoesFinais.getLarguraCm().compareTo(larguraTotalLoteCm) > 0) {
            throw new RegraNegocioException(
                    "Largura de corte final (" + dimensoesFinais.getLarguraCm() +
                            "cm) maior que largura do lote (" + larguraTotalLoteCm + "cm)."
            );
        }

        BigDecimal saldoAtualMetros = movimentacaoEstoqueLoteRepository.findSaldoByLote(lote);
        BigDecimal comprimentoDeCorteMetros = dimensoesFinais.getComprimentoCm()
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

        if (comprimentoDeCorteMetros.compareTo(saldoAtualMetros) > 0) {
            throw new RegraNegocioException(
                    "Saldo insuficiente. Necessário: " + comprimentoDeCorteMetros + "m, Disponível: " + saldoAtualMetros + "m."
            );
        }
    }

    private void registrarEntradaProdutoAcabado(OrdemDeCorteRequestDTO requestDTO, Produto produto) {
        MovimentacaoEstoqueProduto entradaProducao = MovimentacaoEstoqueProduto.builder()
                .produto(produto)
                .tipo(TipoMovimentacaoProduto.ENTRADA_PRODUCAO)
                .quantidade(requestDTO.getQuantidadeProduzida())
                .motivo("Produzido via Ordem de Corte. Consumiu Lote ID: " + requestDTO.getLotePrincipalId())
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
        Map<String, Object> novosAtributos = Map.of("larguraMm", larguraSobraCm.multiply(new BigDecimal("10")).intValue());
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

    private Produto findProdutoById(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ProdutoNotFoundException(id));
    }

    private BigDecimal getLarguraEmCm(Map<String, Object> atributos) {
        Object larguraMmObj = atributos.get("larguraMm");
        if (!(larguraMmObj instanceof Number)) {
            throw new RegraNegocioException("O lote não possui 'larguraMm' numérico.");
        }
        return new BigDecimal(((Number) larguraMmObj).intValue()).divide(new BigDecimal("10"), 2, RoundingMode.HALF_UP);
    }
}